package tn.esprit.canvabna.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.canvabna.dto.StructureRequest;
import tn.esprit.canvabna.dto.StructureResponse;
import tn.esprit.canvabna.entity.Structure;
import tn.esprit.canvabna.exception.ResourceNotFoundException;
import tn.esprit.canvabna.mapper.StructureMapper;
import tn.esprit.canvabna.repository.StructureRepository;

import java.util.List;

/**
 * Service de gestion des structures organisationnelles.
 * CRUD complet avec gestion des erreurs via {@link ResourceNotFoundException}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StructureService {

    private final StructureRepository structureRepository;
    private final StructureMapper structureMapper;

    @Transactional(readOnly = true)
    public List<StructureResponse> findAll() {
        log.debug("Fetching all structures");
        return structureRepository.findAll()
                .stream()
                .map(structureMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public StructureResponse findById(final Long id) {
        log.debug("Fetching structure by id: {}", id);
        return structureRepository.findById(id)
                .map(structureMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Structure", "id", id));
    }

    @Transactional
    public StructureResponse create(final StructureRequest request) {
        log.info("Creating structure with code: {}", request.getCode());
        if (structureRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Le code structure '" + request.getCode() + "' existe déjà");
        }
        final Structure structure = structureMapper.toEntity(request);
        return structureMapper.toResponse(structureRepository.save(structure));
    }

    @Transactional
    public StructureResponse update(final Long id, final StructureRequest request) {
        log.info("Updating structure id: {}", id);
        final Structure structure = structureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Structure", "id", id));
        structure.setNom(request.getNom());
        structure.setCode(request.getCode());
        return structureMapper.toResponse(structureRepository.save(structure));
    }

    @Transactional
    public void delete(final Long id) {
        log.info("Deleting structure id: {}", id);
        if (!structureRepository.existsById(id)) {
            throw new ResourceNotFoundException("Structure", "id", id);
        }
        structureRepository.deleteById(id);
    }
}
