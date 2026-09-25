package tn.esprit.canvabna.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.canvabna.dto.ReferenceItemDto;
import tn.esprit.canvabna.entity.ObjetCreditRef;
import tn.esprit.canvabna.entity.ProduitRef;
import tn.esprit.canvabna.repository.ObjetCreditRefRepository;
import tn.esprit.canvabna.repository.ProduitRefRepository;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReferenceDataService {

    private final ProduitRefRepository produitRefRepository;
    private final ObjetCreditRefRepository objetCreditRefRepository;

    @Transactional(readOnly = true)
    public List<ReferenceItemDto> getAllProduits() {
        return produitRefRepository.findAll().stream()
                .map(p -> ReferenceItemDto.builder()
                        .id(p.getId())
                        .code(p.getCode())
                        .libelle(p.getLibelle())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReferenceItemDto> getAllObjetsCredit() {
        return objetCreditRefRepository.findAll().stream()
                .map(o -> ReferenceItemDto.builder()
                        .id(o.getId())
                        .code(o.getCode())
                        .libelle(o.getLibelle())
                        .build())
                .collect(Collectors.toList());
    }

    public List<String> getPeriodicites() {
        return Arrays.asList("Mensuelle", "Trimestrielle", "Semestrielle", "Annuelle");
    }

    public List<String> getTypesTaux() {
        return Arrays.asList("Taux Variable", "Taux Fixe");
    }

    public List<String> getTypesCommission() {
        return Arrays.asList("Forfaitaire", "Proportionnelle");
    }

    @Transactional
    public void seedProduitIfMissing(String code, String libelle) {
        if (!produitRefRepository.existsByCode(code)) {
            produitRefRepository.save(ProduitRef.builder()
                    .code(code)
                    .libelle(libelle)
                    .build());
        }
    }

    @Transactional
    public void seedObjetCreditIfMissing(String code, String libelle) {
        if (!objetCreditRefRepository.existsByCode(code)) {
            objetCreditRefRepository.save(ObjetCreditRef.builder()
                    .code(code)
                    .libelle(libelle)
                    .build());
        }
    }
}
