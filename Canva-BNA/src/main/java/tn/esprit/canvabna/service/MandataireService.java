package tn.esprit.canvabna.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.canvabna.dto.MandataireRequest;
import tn.esprit.canvabna.dto.MandataireResponse;
import tn.esprit.canvabna.entity.Compte;
import tn.esprit.canvabna.entity.CompteId;
import tn.esprit.canvabna.entity.Mandataire;
import tn.esprit.canvabna.exception.ResourceNotFoundException;
import tn.esprit.canvabna.mapper.MandataireMapper;
import tn.esprit.canvabna.repository.CompteRepository;
import tn.esprit.canvabna.repository.MandataireRepository;

import java.util.List;

/**
 * Service de gestion des mandataires bancaires.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MandataireService {

    private final MandataireRepository mandataireRepository;
    private final CompteRepository compteRepository;
    private final MandataireMapper mandataireMapper;

    @Transactional(readOnly = true)
    public List<MandataireResponse> findAll() {
        return mandataireRepository.findAll()
                .stream()
                .map(mandataireMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MandataireResponse findById(final Long id) {
        return mandataireRepository.findById(id)
                .map(mandataireMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Mandataire", "id", id));
    }

    @Transactional(readOnly = true)
    public List<MandataireResponse> findByCompte(final String codeGuichet, final String codeProduit, final String numCompte) {
        log.debug("Fetching mandataires for compte: {}-{}-{}", codeGuichet, codeProduit, numCompte);
        final CompteId id = new CompteId(codeGuichet, codeProduit, numCompte);
        if (!compteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Compte", "id", codeGuichet + "-" + codeProduit + "-" + numCompte);
        }
        return mandataireRepository.findByCompte_Id_CodeGuichetAndCompte_Id_CodeProduitAndCompte_Id_NumCompte(codeGuichet, codeProduit, numCompte)
                .stream()
                .map(mandataireMapper::toResponse)
                .toList();
    }

    @Transactional
    public MandataireResponse create(final MandataireRequest request) {
        log.info("Creating mandataire '{}' for compte: {}-{}-{}", 
                request.getMandant(), request.getCompteCodeGuichet(), request.getCompteCodeProduit(), request.getCompteNumCompte());
        
        final CompteId compteId = new CompteId(request.getCompteCodeGuichet(), request.getCompteCodeProduit(), request.getCompteNumCompte());
        final Compte compte = compteRepository.findById(compteId)
                .orElseThrow(() -> new ResourceNotFoundException("Compte", "id", 
                        request.getCompteCodeGuichet() + "-" + request.getCompteCodeProduit() + "-" + request.getCompteNumCompte()));

        final Mandataire mandataire = Mandataire.builder()
                .numMandat(request.getNumMandat())
                .numDemande(request.getNumDemande())
                .typeMandat(request.getTypeMandat())
                .agence(request.getAgence())
                .mandant(request.getMandant())
                .dateCreation(request.getDateCreation())
                .dateDebut(request.getDateDebut())
                .dateFin(request.getDateFin())
                .compte(compte)
                .build();

        return mandataireMapper.toResponse(mandataireRepository.save(mandataire));
    }

    @Transactional
    public MandataireResponse update(final Long id, final MandataireRequest request) {
        log.info("Updating mandataire id: {}", id);
        final Mandataire mandataire = mandataireRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mandataire", "id", id));

        mandataire.setNumMandat(request.getNumMandat());
        mandataire.setNumDemande(request.getNumDemande());
        mandataire.setTypeMandat(request.getTypeMandat());
        mandataire.setAgence(request.getAgence());
        mandataire.setMandant(request.getMandant());
        mandataire.setDateCreation(request.getDateCreation());
        mandataire.setDateDebut(request.getDateDebut());
        mandataire.setDateFin(request.getDateFin());

        final CompteId newCompteId = new CompteId(request.getCompteCodeGuichet(), request.getCompteCodeProduit(), request.getCompteNumCompte());
        if (mandataire.getCompte() == null || !mandataire.getCompte().getId().equals(newCompteId)) {
            final Compte newCompte = compteRepository.findById(newCompteId)
                    .orElseThrow(() -> new ResourceNotFoundException("Compte", "id", 
                            request.getCompteCodeGuichet() + "-" + request.getCompteCodeProduit() + "-" + request.getCompteNumCompte()));
            mandataire.setCompte(newCompte);
        }

        return mandataireMapper.toResponse(mandataireRepository.save(mandataire));
    }

    @Transactional
    public void delete(final Long id) {
        log.info("Deleting mandataire id: {}", id);
        if (!mandataireRepository.existsById(id)) {
            throw new ResourceNotFoundException("Mandataire", "id", id);
        }
        mandataireRepository.deleteById(id);
    }
}
