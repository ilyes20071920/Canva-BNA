package tn.esprit.canvabna.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.canvabna.dto.ActionnaireRequest;
import tn.esprit.canvabna.dto.ActionnaireResponse;
import tn.esprit.canvabna.entity.Actionnaire;
import tn.esprit.canvabna.entity.Client;
import tn.esprit.canvabna.exception.ResourceNotFoundException;
import tn.esprit.canvabna.mapper.ActionnaireMapper;
import tn.esprit.canvabna.repository.ActionnaireRepository;
import tn.esprit.canvabna.repository.ClientRepository;

import java.util.List;

/**
 * Service de gestion des actionnaires/associés du capital social.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActionnaireService {

    private final ActionnaireRepository actionnaireRepository;
    private final ClientRepository clientRepository;
    private final ActionnaireMapper actionnaireMapper;

    @Transactional(readOnly = true)
    public List<ActionnaireResponse> findAll() {
        return actionnaireRepository.findAll()
                .stream()
                .map(actionnaireMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ActionnaireResponse findById(final Long id) {
        return actionnaireRepository.findById(id)
                .map(actionnaireMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Actionnaire", "id", id));
    }

    @Transactional(readOnly = true)
    public List<ActionnaireResponse> findByClientId(final Long clientId) {
        log.debug("Fetching actionnaires for client id: {}", clientId);
        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Client", "id", clientId);
        }
        return actionnaireRepository.findByClientId(clientId)
                .stream()
                .map(actionnaireMapper::toResponse)
                .toList();
    }

    @Transactional
    public ActionnaireResponse create(final ActionnaireRequest request) {
        log.info("Creating actionnaire '{}' for client id: {}", request.getNom(), request.getClientId());
        final Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", request.getClientId()));

        final Actionnaire actionnaire = Actionnaire.builder()
                .nom(request.getNom())
                .nombreActions(request.getNombreActions())
                .montant(request.getMontant())
                .pourcentageActions(request.getPourcentageActions())
                .client(client)
                .build();

        return actionnaireMapper.toResponse(actionnaireRepository.save(actionnaire));
    }

    @Transactional
    public ActionnaireResponse update(final Long id, final ActionnaireRequest request) {
        log.info("Updating actionnaire id: {}", id);
        final Actionnaire actionnaire = actionnaireRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Actionnaire", "id", id));

        actionnaire.setNom(request.getNom());
        actionnaire.setNombreActions(request.getNombreActions());
        actionnaire.setMontant(request.getMontant());
        actionnaire.setPourcentageActions(request.getPourcentageActions());

        if (!actionnaire.getClient().getId().equals(request.getClientId())) {
            final Client newClient = clientRepository.findById(request.getClientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client", "id", request.getClientId()));
            actionnaire.setClient(newClient);
        }

        return actionnaireMapper.toResponse(actionnaireRepository.save(actionnaire));
    }

    @Transactional
    public void delete(final Long id) {
        log.info("Deleting actionnaire id: {}", id);
        if (!actionnaireRepository.existsById(id)) {
            throw new ResourceNotFoundException("Actionnaire", "id", id);
        }
        actionnaireRepository.deleteById(id);
    }
}
