import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';

export interface Structure {
  id?: number;
  nom: string;
  code: string;
}

export interface Actionnaire {
  id?: number;
  nom: string;
  nombreActions: number;
  montant?: number;
  pourcentageActions?: number;
  clientId?: number;
}

export interface Mandataire {
  id?: number;
  numMandat?: string;
  numDemande?: string;
  typeMandat?: string;
  agence?: string;
  mandant?: string;
  dateCreation?: string;
  dateDebut?: string;
  dateFin?: string;
  compteCodeGuichet?: string;
  compteCodeProduit?: string;
  compteNumCompte?: string;
}

export interface Compte {
  codeGuichet: string;
  codeProduit: string;
  numCompte: string;
  numeroCompteComplet?: string;
  agence?: string;
  clientId?: number;
  mandataires?: Mandataire[];
}

export interface CompteValideResponse {
  numeroCompte: string;
  dateOuverture?: string | null;
  typeAutorisation?: string | null;
  autorisation?: number | null;
  dateAutorisation?: string | null;
  solde?: number | null;
}

export interface SoldeParTypeResponse {
  typeCompte: string;
  soldeAlgebrique: number;
}

export interface EngagementDetailResponse {
  formeCredit: string;
  numCompte?: string | null;
  autorise?: number | null;
  echeance?: string | null;
  encours?: number | null;
  impayes?: number | null;
  ir?: number | null;
  anciennete?: string | null;
  detailsImpayes?: string | null;
}

export interface EngagementBnaRowResponse {
  code: string;
  forme: string;
  autorise?: number | null;
  echeance?: string | null;
  encours?: number | null;
  impayes?: number | null;
  ir?: number | null;
  ancienneteImpayes?: string | null;
  detailsTitle?: string;
  subtotalLabel?: string;
  details?: EngagementDetailResponse[];
}

export interface EngagementsActiviteResponse {
  comptes: CompteValideResponse[];
  soldesDisponibles: boolean;
  soldesParType: SoldeParTypeResponse[];
  engagementsBna?: EngagementBnaRowResponse[];
}

export interface ActiviteAnneeResponse {
  annee: number;
  totalMouvement: number;
  ca: number;
  partConfiee: number;
  partEngagement: number;
}

export interface ActiviteResponse {
  dataAvailable: boolean;
  activities: ActiviteAnneeResponse[];
}

export interface Client {
  id?: number;
  identifiant: string;
  groupe: string;
  relation: string;
  activite?: string;
  segment?: string;
  siegeSocial?: string;
  secteur?: string;
  dateEntreeRelation?: string;
  formeJuridique?: string;
  capitalSocial?: number;
  agence?: string;
  directionRegionale?: string;
  structure?: Structure;
  structureId?: number;
  actionnaires?: Actionnaire[];
  comptes?: Compte[];
}

@Component({
  selector: 'app-fiche-client',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './fiche-client.component.html',
  styleUrl: './fiche-client.component.scss'
})
export class FicheClientComponent implements OnInit {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8089/api';

  // State
  clients: Client[] = [];
  selectedClient: Client | null = null;
  structures: Structure[] = [];
  
  // Tabs & Stepper
  activeTab: string = 'Signalétique';
  activeStep: string = 'Présentation Générale'; // 'Présentation Générale' | 'Dossier Juridique' | 'Profil de Risque'

  // Engagements & Activité Stepper
  engagementActiveStep: string = 'Compte et dépôts'; // 'Compte et dépôts' | 'Engagements (BCT, BNA)' | 'Activité'
  engagementsActiviteData: EngagementsActiviteResponse | null = null;
  isLoadingEngagements: boolean = false;
  expandedEngagementCode: string | null = null;
  
  activiteData: ActiviteResponse | null = null;
  isLoadingActivite: boolean = false;

  // Selected Compte for Dossier Juridique (matches numeroCompteComplet)
  selectedCompteId: string | null = null;
  selectedCompte: Compte | null = null;

  // Search/Load
  searchIdentifiant: string = '';
  
  // Modals for editing/creating nested items
  showAddClientModal = false;
  showAddCompteModal = false;
  showAddActionnaireModal = false;
  showAddMandataireModal = false;

  // Form payloads
  newClientPayload: any = {
    identifiant: '',
    groupe: '',
    relation: '',
    activite: '',
    segment: 'Corporates',
    siegeSocial: '',
    secteur: '',
    dateEntreeRelation: '',
    formeJuridique: '',
    capitalSocial: 0,
    agence: '',
    directionRegionale: '',
    structureId: null
  };

  newComptePayload = {
    codeGuichet: '',
    codeProduit: '',
    numCompte: '',
    agence: ''
  };

  newActionnairePayload = {
    nom: '',
    nombreActions: 0,
    montant: 0,
    pourcentageActions: 0
  };

  newMandatairePayload = {
    numMandat: '',
    numDemande: '',
    typeMandat: 'General',
    agence: '',
    mandant: '',
    dateCreation: '',
    dateDebut: '',
    dateFin: ''
  };

  // Status/Messages
  successMessage: string = '';
  errorMessage: string = '';

  ngOnInit(): void {
    this.loadStructures();
    this.loadClients();
  }

  getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  loadStructures(): void {
    this.http.get<Structure[]>(`${this.baseUrl}/structures`, { headers: this.getHeaders() })
      .subscribe({
        next: (data) => this.structures = data,
        error: (err) => console.error('Error loading structures', err)
      });
  }

  loadClients(): void {
    this.http.get<Client[]>(`${this.baseUrl}/clients`, { headers: this.getHeaders() })
      .subscribe({
        next: (data) => {
          this.clients = data;
          if (this.clients.length > 0 && !this.selectedClient) {
            this.selectClient(this.clients[0]);
          }
        },
        error: (err) => console.error('Error loading clients', err)
      });
  }

  searchClient(): void {
    if (!this.searchIdentifiant.trim()) return;
    this.clearMessages();
    this.http.get<Client>(`${this.baseUrl}/clients/identifiant/${this.searchIdentifiant.trim()}`, { headers: this.getHeaders() })
      .subscribe({
        next: (client) => {
          this.selectedClient = client;
          if (client.structure) {
            this.selectedClient.structureId = client.structure.id;
          }
          this.successMessage = 'Client chargé avec succès.';
          this.resolveSelectedCompte();
        },
        error: (err) => {
          this.errorMessage = 'Client introuvable.';
          console.error('Client search error', err);
        }
      });
  }

  private successTimer: any = null;

  selectClient(client: Client, keepSuccessMessage: boolean = false): void {
    if (!keepSuccessMessage) {
      this.clearMessages();
    } else {
      this.errorMessage = '';
    }
    this.http.get<Client>(`${this.baseUrl}/clients/${client.id}`, { headers: this.getHeaders() })
      .subscribe({
        next: (fullClient) => {
          this.selectedClient = fullClient;
          if (fullClient.structure) {
            this.selectedClient.structureId = fullClient.structure.id;
          }
          this.resolveSelectedCompte();
        },
        error: (err) => console.error('Error fetching client details', err)
      });
  }

  resolveSelectedCompte(): void {
    if (this.selectedClient && this.selectedClient.comptes && this.selectedClient.comptes.length > 0) {
      this.selectedCompte = this.selectedClient.comptes[0];
      this.selectedCompteId = this.selectedCompte.numeroCompteComplet || null;
    } else {
      this.selectedCompte = null;
      this.selectedCompteId = null;
    }
  }

  onCompteChange(): void {
    if (this.selectedClient && this.selectedClient.comptes) {
      this.selectedCompte = this.selectedClient.comptes.find(c => c.numeroCompteComplet === this.selectedCompteId) || null;
    }
  }

  setActiveTab(tab: string): void {
    this.activeTab = tab;
    if (tab === 'Engagements') {
      this.loadEngagementsActivite();
    }
  }

  setEngagementActiveStep(step: string): void {
    this.engagementActiveStep = step;
    if (step === 'Activité') {
      this.loadActivite();
    }
  }

  toggleEngagementRow(code: string): void {
    this.expandedEngagementCode = this.expandedEngagementCode === code ? null : code;
  }

  loadActivite(): void {
    if (!this.selectedClient || !this.selectedClient.id) return;
    this.isLoadingActivite = true;
    this.activiteData = null;
    
    this.http.get<ActiviteResponse>(`${this.baseUrl}/clients/${this.selectedClient.id}/engagements-activite/activites`, { headers: this.getHeaders() })
      .subscribe({
        next: (data) => {
          this.activiteData = data;
          this.isLoadingActivite = false;
        },
        error: (err) => {
          console.error('Error loading activite', err);
          this.errorMessage = 'Erreur lors du chargement des activités.';
          this.isLoadingActivite = false;
        }
      });
  }

  loadEngagementsActivite(): void {
    if (!this.selectedClient || !this.selectedClient.id) return;
    this.isLoadingEngagements = true;
    this.engagementsActiviteData = null;
    
    this.http.get<EngagementsActiviteResponse>(`${this.baseUrl}/clients/${this.selectedClient.id}/engagements-activite/comptes-depots`, { headers: this.getHeaders() })
      .subscribe({
        next: (data) => {
          this.engagementsActiviteData = data;
          this.isLoadingEngagements = false;
        },
        error: (err) => {
          console.error('Error loading engagements', err);
          this.errorMessage = 'Erreur lors du chargement des engagements et de l\'activité.';
          this.isLoadingEngagements = false;
        }
      });
  }

  setActiveStep(step: string): void {
    this.activeStep = step;
  }

  clearMessages(): void {
    if (this.successTimer) {
      clearTimeout(this.successTimer);
      this.successTimer = null;
    }
    this.successMessage = '';
    this.errorMessage = '';
  }

  /** Affiche un message de succès stable qui reste 6 secondes sans clignoter. */
  showSuccess(msg: string): void {
    if (this.successTimer) {
      clearTimeout(this.successTimer);
    }
    this.successMessage = msg;
    this.successTimer = setTimeout(() => {
      this.successMessage = '';
      this.successTimer = null;
    }, 2000);
  }

  // ================================================================ Client CRUD
  openAddClient(): void {
    this.newClientPayload = {
      identifiant: '',
      groupe: '',
      relation: '',
      activite: '',
      segment: 'Corporates',
      siegeSocial: '',
      secteur: '',
      dateEntreeRelation: new Date().toISOString().split('T')[0],
      formeJuridique: 'Societe A Responsabilite Limitee',
      capitalSocial: 0,
      agence: '',
      directionRegionale: '',
      structureId: this.structures.length > 0 ? this.structures[0].id : null
    };
    this.showAddClientModal = true;
  }

  saveClient(): void {
    this.clearMessages();
    // Ensure dateEntreeRelation is not empty string (Jackson cannot parse "" as LocalDate)
    const payload = {
      ...this.newClientPayload,
      dateEntreeRelation: this.newClientPayload.dateEntreeRelation || null
    };
    this.http.post<Client>(`${this.baseUrl}/clients`, payload, { headers: this.getHeaders() })
      .subscribe({
        next: (created) => {
          this.showSuccess('Fiche Client créée avec succès.');
          this.showAddClientModal = false;
          this.loadClients();
          // Sélectionner le client créé depuis le serveur pour avoir les données complètes
          if (created.id) this.selectClient(created);
        },
        error: (err) => {
          this.errorMessage = `Erreur création client : ${this.formatError(err)}`;
          console.error('saveClient error:', err);
        }
      });
  }

  updateClient(): void {
    this.clearMessages();
    this.showSuccess('Canevas enregistré avec succès !');

    if (!this.selectedClient) return;

    // Ensure structureId is always a number (not a string from [value] binding)
    const structureIdRaw = this.selectedClient.structureId;
    const structureId = structureIdRaw != null ? Number(structureIdRaw) : null;

    const payload = {
      identifiant: this.selectedClient.identifiant,
      groupe: this.selectedClient.groupe,
      relation: this.selectedClient.relation || this.selectedClient.groupe,
      activite: this.selectedClient.activite || '',
      segment: this.selectedClient.segment || 'Corporates',
      siegeSocial: this.selectedClient.siegeSocial || '',
      secteur: this.selectedClient.secteur || '',
      dateEntreeRelation: this.selectedClient.dateEntreeRelation || null,
      formeJuridique: this.selectedClient.formeJuridique || '',
      capitalSocial: this.selectedClient.capitalSocial || 0,
      agence: this.selectedClient.agence || '',
      directionRegionale: this.selectedClient.directionRegionale || '',
      structureId: (structureId && !isNaN(structureId)) ? structureId : null
    };

    const clientToRefresh = { ...this.selectedClient };

    if (this.selectedClient.id) {
      this.http.put<Client>(`${this.baseUrl}/clients/${this.selectedClient.id}`, payload, { headers: this.getHeaders() })
        .subscribe({
          next: () => {
            this.loadClients();
            this.selectClient(clientToRefresh, true);
          },
          error: (err) => {
            console.error('updateClient error:', err);
          }
        });
    }
  }

  // ================================================================ Nested creations

  openAddCompte(): void {
    this.newComptePayload = { codeGuichet: '', codeProduit: '', numCompte: '', agence: '' };
    this.showAddCompteModal = true;
  }

  saveCompte(): void {
    if (!this.selectedClient) return;
    const payload = {
      ...this.newComptePayload,
      clientId: this.selectedClient.id
    };
    this.http.post<Compte>(`${this.baseUrl}/comptes`, payload, { headers: this.getHeaders() })
      .subscribe({
        next: () => {
          this.showAddCompteModal = false;
          this.showSuccess('Compte bancaire ajouté avec succès.');
          if (this.selectedClient) this.selectClient(this.selectedClient);
        },
        error: (err) => {
          this.errorMessage = `Erreur ajout compte : ${this.formatError(err)}`;
          console.error('saveCompte error:', err);
        }
      });
  }

  openAddActionnaire(): void {
    this.newActionnairePayload = { nom: '', nombreActions: 0, montant: 0, pourcentageActions: 0 };
    this.showAddActionnaireModal = true;
  }

  saveActionnaire(): void {
    if (!this.selectedClient) return;
    const payload = {
      ...this.newActionnairePayload,
      clientId: this.selectedClient.id
    };
    this.http.post<Actionnaire>(`${this.baseUrl}/actionnaires`, payload, { headers: this.getHeaders() })
      .subscribe({
        next: () => {
          this.showAddActionnaireModal = false;
          this.showSuccess('Actionnaire ajouté avec succès.');
          if (this.selectedClient) this.selectClient(this.selectedClient);
        },
        error: (err) => {
          this.errorMessage = `Erreur ajout actionnaire : ${this.formatError(err)}`;
          console.error('saveActionnaire error:', err);
        }
      });
  }

  openAddMandataire(): void {
    if (!this.selectedCompte) {
      alert("Veuillez d'abord sélectionner ou ajouter un compte.");
      return;
    }
    this.newMandatairePayload = {
      numMandat: '',
      numDemande: '',
      typeMandat: 'General',
      agence: this.selectedCompte.agence || '',
      mandant: '',
      dateCreation: new Date().toISOString().split('T')[0],
      dateDebut: '',
      dateFin: ''
    };
    this.showAddMandataireModal = true;
  }

  saveMandataire(): void {
    if (!this.selectedCompte) {
      this.errorMessage = 'Veuillez sélectionner un compte pour ce mandataire.';
      return;
    }
    const payload = {
      ...this.newMandatairePayload,
      compteCodeGuichet: this.selectedCompte.codeGuichet,
      compteCodeProduit: this.selectedCompte.codeProduit,
      compteNumCompte: this.selectedCompte.numCompte,
      dateCreation: this.newMandatairePayload.dateCreation || null,
      dateDebut: this.newMandatairePayload.dateDebut || null,
      dateFin: this.newMandatairePayload.dateFin || null
    };

    this.http.post<Mandataire>(`${this.baseUrl}/mandataires`, payload, { headers: this.getHeaders() })
      .subscribe({
        next: (res) => {
          this.showSuccess('Mandataire ajouté avec succès.');
          this.showAddMandataireModal = false;
          this.newMandatairePayload = {
            numMandat: '', numDemande: '', typeMandat: 'General', agence: '', mandant: '',
            dateCreation: '', dateDebut: '', dateFin: ''
          };
          this.selectClient(this.selectedClient!);
        },
        error: (err) => {
          this.errorMessage = `Erreur ajout mandataire : ${this.formatError(err)}`;
          console.error('saveMandataire error:', err);
        }
      });
  }

  deleteMandataire(id: number): void {
    if (!confirm('Voulez-vous vraiment supprimer ce mandataire ?')) return;
    this.http.delete(`${this.baseUrl}/mandataires/${id}`, { headers: this.getHeaders() })
      .subscribe({
        next: () => {
          this.showSuccess('Mandataire supprimé avec succès.');
          this.selectClient(this.selectedClient!);
        },
        error: (err) => {
          this.errorMessage = `Erreur suppression mandataire : ${this.formatError(err)}`;
        }
      });
  }

  /**
   * Extrait et formate les erreurs (incluant les fieldErrors de validation) depuis l'objet HttpErrorResponse.
   */
  private formatError(err: any): string {
    if (err?.error?.fieldErrors) {
      const fieldErrors = err.error.fieldErrors;
      const firstKey = Object.keys(fieldErrors)[0];
      return `${fieldErrors[firstKey]} (${firstKey})`;
    }
    return err?.error?.message || err?.error || err?.message || 'Erreur inconnue';
  }
}
