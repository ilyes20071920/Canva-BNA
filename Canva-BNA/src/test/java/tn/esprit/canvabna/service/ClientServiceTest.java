package tn.esprit.canvabna.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.canvabna.dto.ClientRequest;
import tn.esprit.canvabna.dto.ClientResponse;
import tn.esprit.canvabna.entity.Client;
import tn.esprit.canvabna.exception.ResourceNotFoundException;
import tn.esprit.canvabna.mapper.ClientMapper;
import tn.esprit.canvabna.repository.ClientRepository;
import tn.esprit.canvabna.repository.StructureRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ClientService}.
 * All dependencies are mocked — no Spring context needed.
 * Follows the same pattern as {@link AuthServiceTest}.
 */
@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;
    @Mock
    private StructureRepository structureRepository;
    @Mock
    private ClientMapper clientMapper;

    @InjectMocks
    private ClientService clientService;

    private Client sampleClient;
    private ClientResponse sampleResponse;
    private ClientRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleClient = Client.builder()
                .id(1L)
                .identifiant("1042986R")
                .groupe("BOUDOKHANE MOKHTAR")
                .relation("STE ENT. BOUDOKHANE MOKHTAR")
                .segment("Corporates")
                .siegeSocial("BLOC 11 CITE OLYMPIQUE TUN 1003")
                .secteur("Sociétés non financières")
                .dateEntreeRelation(LocalDate.of(2008, 10, 20))
                .formeJuridique("Societe A Responsabilite Limitee")
                .capitalSocial(BigDecimal.ZERO)
                .directionRegionale("DIR.REG. TUNIS I")
                .build();

        sampleResponse = ClientResponse.builder()
                .id(1L)
                .identifiant("1042986R")
                .groupe("BOUDOKHANE MOKHTAR")
                .relation("STE ENT. BOUDOKHANE MOKHTAR")
                .segment("Corporates")
                .build();

        sampleRequest = new ClientRequest();
        sampleRequest.setIdentifiant("1042986R");
        sampleRequest.setGroupe("BOUDOKHANE MOKHTAR");
        sampleRequest.setRelation("STE ENT. BOUDOKHANE MOKHTAR");
        sampleRequest.setSegment("Corporates");
        sampleRequest.setSiegeSocial("BLOC 11 CITE OLYMPIQUE TUN 1003");
        sampleRequest.setSecteur("Sociétés non financières");
        sampleRequest.setDateEntreeRelation(LocalDate.of(2008, 10, 20));
        sampleRequest.setFormeJuridique("Societe A Responsabilite Limitee");
        sampleRequest.setCapitalSocial(BigDecimal.ZERO);
        sampleRequest.setDirectionRegionale("DIR.REG. TUNIS I");
    }

    // ---------------------------------------------------------------- findAll

    @Test
    @DisplayName("findAll — should return list of all clients")
    void findAll_shouldReturnAllClients() {
        when(clientRepository.findAll()).thenReturn(List.of(sampleClient));
        when(clientMapper.toResponse(sampleClient)).thenReturn(sampleResponse);

        final List<ClientResponse> result = clientService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIdentifiant()).isEqualTo("1042986R");
        verify(clientRepository).findAll();
    }

    // ---------------------------------------------------------------- findById

    @Test
    @DisplayName("findById — should return client when found")
    void findById_shouldReturnClient_whenFound() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(sampleClient));
        when(clientMapper.toResponse(sampleClient)).thenReturn(sampleResponse);

        final ClientResponse result = clientService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getIdentifiant()).isEqualTo("1042986R");
        assertThat(result.getGroupe()).isEqualTo("BOUDOKHANE MOKHTAR");
    }

    @Test
    @DisplayName("findById — should throw ResourceNotFoundException when not found")
    void findById_shouldThrowResourceNotFoundException_whenNotFound() {
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Client")
                .hasMessageContaining("999");
    }

    // ---------------------------------------------------------------- create

    @Test
    @DisplayName("create — should return ClientResponse on successful creation")
    void create_shouldReturnClientResponse_whenIdentifiantIsUnique() {
        when(clientRepository.existsByIdentifiant("1042986R")).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenReturn(sampleClient);
        when(clientMapper.toResponse(sampleClient)).thenReturn(sampleResponse);

        final ClientResponse result = clientService.create(sampleRequest);

        assertThat(result).isNotNull();
        assertThat(result.getIdentifiant()).isEqualTo("1042986R");
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    @DisplayName("create — should throw IllegalArgumentException when identifiant already exists")
    void create_shouldThrowIllegalArgumentException_whenIdentifiantIsDuplicate() {
        when(clientRepository.existsByIdentifiant("1042986R")).thenReturn(true);

        assertThatThrownBy(() -> clientService.create(sampleRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("1042986R");

        verify(clientRepository, never()).save(any());
    }

    // ---------------------------------------------------------------- update

    @Test
    @DisplayName("update — should return updated ClientResponse when found")
    void update_shouldReturnUpdatedClientResponse_whenFound() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(sampleClient));
        when(clientRepository.save(any(Client.class))).thenReturn(sampleClient);
        when(clientMapper.toResponse(sampleClient)).thenReturn(sampleResponse);

        final ClientResponse result = clientService.update(1L, sampleRequest);

        assertThat(result).isNotNull();
        verify(clientRepository).findById(1L);
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    @DisplayName("update — should throw ResourceNotFoundException when not found")
    void update_shouldThrowResourceNotFoundException_whenNotFound() {
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.update(999L, sampleRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Client")
                .hasMessageContaining("999");

        verify(clientRepository, never()).save(any());
    }

    // ---------------------------------------------------------------- delete

    @Test
    @DisplayName("delete — should delete client when found")
    void delete_shouldDeleteClient_whenFound() {
        when(clientRepository.existsById(1L)).thenReturn(true);

        clientService.delete(1L);

        verify(clientRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete — should throw ResourceNotFoundException when not found")
    void delete_shouldThrowResourceNotFoundException_whenNotFound() {
        when(clientRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> clientService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Client")
                .hasMessageContaining("999");

        verify(clientRepository, never()).deleteById(any());
    }

    // ---------------------------------------------------------------- findByIdentifiant

    @Test
    @DisplayName("findByIdentifiant — should return client when found")
    void findByIdentifiant_shouldReturnClient_whenFound() {
        when(clientRepository.findByIdentifiant("1042986R")).thenReturn(Optional.of(sampleClient));
        when(clientMapper.toResponse(sampleClient)).thenReturn(sampleResponse);

        final ClientResponse result = clientService.findByIdentifiant("1042986R");

        assertThat(result).isNotNull();
        assertThat(result.getIdentifiant()).isEqualTo("1042986R");
    }

    @Test
    @DisplayName("findByIdentifiant — should throw ResourceNotFoundException when not found")
    void findByIdentifiant_shouldThrowResourceNotFoundException_whenNotFound() {
        when(clientRepository.findByIdentifiant("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.findByIdentifiant("UNKNOWN"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Client")
                .hasMessageContaining("UNKNOWN");
    }
}
