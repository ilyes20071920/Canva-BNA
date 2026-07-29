package tn.esprit.canvabna.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.canvabna.dto.CompteRequest;
import tn.esprit.canvabna.dto.CompteResponse;
import tn.esprit.canvabna.entity.Client;
import tn.esprit.canvabna.entity.Compte;
import tn.esprit.canvabna.entity.CompteId;
import tn.esprit.canvabna.exception.ResourceNotFoundException;
import tn.esprit.canvabna.mapper.CompteMapper;
import tn.esprit.canvabna.repository.ClientRepository;
import tn.esprit.canvabna.repository.CompteRepository;

import java.util.List;

/**
 * Service de gestion des comptes bancaires.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompteService {

    private final CompteRepository compteRepository;
    private final ClientRepository clientRepository;
    private final CompteMapper compteMapper;

    @Transactional(readOnly = true)
    public List<CompteResponse> findAll() {
        return compteRepository.findAll()
                .stream()
                .map(compteMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CompteResponse findById(final String codeGuichet, final String codeProduit, final String numCompte) {
        final CompteId id = new CompteId(codeGuichet, codeProduit, numCompte);
        return compteRepository.findById(id)
                .map(compteMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Compte", "id", codeGuichet + "-" + codeProduit + "-" + numCompte));
    }

    @Transactional(readOnly = true)
    public List<CompteResponse> findByClientId(final Long clientId) {
        log.debug("Fetching comptes for client id: {}", clientId);
        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Client", "id", clientId);
        }
        return compteRepository.findByClientId(clientId)
                .stream()
                .map(compteMapper::toResponse)
                .toList();
    }

    @Transactional
    public CompteResponse create(final CompteRequest request) {
        log.info("Creating compte '{}-{}-{}' for client id: {}", 
                request.getCodeGuichet(), request.getCodeProduit(), request.getNumCompte(), request.getClientId());
        
        final CompteId id = new CompteId(request.getCodeGuichet(), request.getCodeProduit(), request.getNumCompte());
        if (compteRepository.existsById(id)) {
            throw new IllegalArgumentException(
                "Le compte bancaire '" + request.getCodeGuichet() + "-" + request.getCodeProduit() + "-" + request.getNumCompte() 
                + "' existe déjà pour ce client. Veuillez utiliser un numéro de compte différent.");
        }
        
        final Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", request.getClientId()));

        final Compte compte = Compte.builder()
                .id(id)
                .agence(request.getAgence())
                .client(client)
                .build();

        return compteMapper.toResponse(compteRepository.save(compte));
    }

    @Transactional
    public CompteResponse update(final String codeGuichet, final String codeProduit, final String numCompte, final CompteRequest request) {
        log.info("Updating compte: {}-{}-{}", codeGuichet, codeProduit, numCompte);
        final CompteId id = new CompteId(codeGuichet, codeProduit, numCompte);
        final Compte compte = compteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compte", "id", codeGuichet + "-" + codeProduit + "-" + numCompte));

        compte.setAgence(request.getAgence());

        if (!compte.getClient().getId().equals(request.getClientId())) {
            final Client newClient = clientRepository.findById(request.getClientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client", "id", request.getClientId()));
            compte.setClient(newClient);
        }

        return compteMapper.toResponse(compteRepository.save(compte));
    }

    @Transactional
    public void delete(final String codeGuichet, final String codeProduit, final String numCompte) {
        log.info("Deleting compte: {}-{}-{}", codeGuichet, codeProduit, numCompte);
        final CompteId id = new CompteId(codeGuichet, codeProduit, numCompte);
        if (!compteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Compte", "id", codeGuichet + "-" + codeProduit + "-" + numCompte);
        }
        compteRepository.deleteById(id);
    }
}
