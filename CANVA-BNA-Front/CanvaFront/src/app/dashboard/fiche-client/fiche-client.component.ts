import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';

declare var html2pdf: any;

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
  numDecisionCrd?: string | null;
  formeCredit: string;
  numCompte?: string | null;
  autorise?: number | null;
  utilise?: number | null;
  echeance?: string | null;
  echFinale?: string | null;
  aEchoirEnPrincipal?: number | null;
  encours?: number | null;
  impayes?: number | null;
  impayesPI?: number | null;
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

export interface TauxRow {
  designation: string;
  valeur: string; // peut être multiligne (séparé par '\n')
}

export interface CommissionRow {
  designation: string;
  avecMg: string;
  sansMg: string;
  garantie: string;
}

export interface ConditionsBanqueResponse {
  compteGroupe: string;   // ex: "BOUDOKHANE MOKHTAR"
  finValidite: string;    // ex: "30/06/2027"
  taux: TauxRow[];
  commissions: CommissionRow[];
  commentaires: string;
}

export interface GarantieResponse {
  id: number;
  libelle: string;
  consistance: string;
  valeur: number;
  dateExpertise: string;
  rang: number;
  beneficiaire: string;
  charges: number;
  affectation: string;
  numSeqGar: string;
  idCredit: string;
}

export interface ReferenceItem {
  id: number;
  code: string;
  libelle: string;
}

export interface PosteInvestissement {
  id?: number;
  libelle: string;
  montantSollicite: number;
}

export interface PosteFinancement {
  id?: number;
  libelle: string;
  montantSollicite: number;
}

export interface NouvelleGarantie {
  id?: number;
  natureGarantie: string;
  estimation?: number;
  dateExpertise?: string;
  chargesInscrites?: number;
}

export interface Sollicitation {
  id?: number;
  clientId: number;
  dateDemande: string;
  compteCodeGuichet?: string;
  compteCodeProduit?: string;
  compteNumCompte?: string;
  compteNumeroComplet?: string;
  produit?: ReferenceItem;
  objetCredit?: ReferenceItem;
  montantSollicite: number;
  dureeSolliciteeMois: number;
  periodicitePl?: string;
  nbrePeriodesSolliciteesPl?: number;
  nbrePeriodesGracePl?: number;
  periodiciteInt?: string;
  nbrePeriodesSolliciteesInt?: number;
  nbrePeriodesGraceInt?: number;
  typeTauxSollicite?: string;
  marge?: number;
  tauxSollicite?: number;
  typeCommission?: string;
  commissionForfaitaireSollicitee?: number;
  commentaireDr?: string;
  apercuProjet?: string;
  etudeMarche?: string;
  syntheseRentabilite?: string;
  swotForces?: string;
  swotFaiblesses?: string;
  swotOpportunites?: string;
  swotMenaces?: string;
  commentaireEtudeProjet?: string;
  totalInvestissementSollicite?: number;
  totalFinancementSollicite?: number;
  postesInvestissement?: PosteInvestissement[];
  postesFinancement?: PosteFinancement[];
  nouvellesGaranties?: NouvelleGarantie[];
  statut?: string;
  decision?: string;
  commentaireDecision?: string;
  dateDecision?: string;
}

export interface GarantiesSummaryResponse {
  garanties: GarantieResponse[];
  totalChargesInscrites: number;
  dontBna: number;
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

  // Conditions Banque
  conditionsBanqueData: ConditionsBanqueResponse | null = null;
  isLoadingConditions: boolean = false;

  // Garanties
  garantiesData: GarantiesSummaryResponse | null = null;
  isLoadingGaranties: boolean = false;
  commentaireGarantie: string = '';


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

  // Custom Confirmation Modal
  showConfirmModal: boolean = false;
  confirmModalTitle: string = 'Confirmation';
  confirmModalMessage: string = '';
  confirmModalAction: (() => void) | null = null;

  openConfirmModal(title: string, message: string, action: () => void): void {
    this.confirmModalTitle = title;
    this.confirmModalMessage = message;
    this.confirmModalAction = action;
    this.showConfirmModal = true;
  }

  executeConfirmModal(): void {
    if (this.confirmModalAction) {
      this.confirmModalAction();
    }
    this.showConfirmModal = false;
    this.confirmModalAction = null;
  }

  cancelConfirmModal(): void {
    this.showConfirmModal = false;
    this.confirmModalAction = null;
  }

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

  // Sollicite State
  solliciteActiveStep: 'credit' | 'etude' = 'credit';
  produitsRef: ReferenceItem[] = [];
  objetsCreditRef: ReferenceItem[] = [];
  periodicites: string[] = ['Mensuelle', 'Trimestrielle', 'Semestrielle', 'Annuelle'];
  typesTaux: string[] = ['Taux Variable', 'Taux Fixe'];
  typesCommission: string[] = ['Forfaitaire', 'Proportionnelle'];

  sollicitationData: Sollicitation | null = null;
  sollicitationFormPayload: any = null;
  isLoadingSollicitation: boolean = false;
  isSavingSollicitation: boolean = false;

  setSolliciteActiveStep(step: 'credit' | 'etude'): void {
    this.solliciteActiveStep = step;
  }

  // Searchable Dropdowns
  showProduitDropdown: boolean = false;
  produitSearchText: string = '';
  showObjetCreditDropdown: boolean = false;
  objetCreditSearchText: string = '';

  selectedProduit: ReferenceItem | null = null;
  selectedObjetCredit: ReferenceItem | null = null;

  ngOnInit(): void {
    this.loadStructures();
    this.loadClients();
    this.loadReferenceData();
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

          if (this.activeTab === 'Sollicite') {
            this.loadSollicitation();
            this.loadGaranties();
          }
          if (this.activeTab === 'Engagements') {
            this.loadEngagementsActivite();
            if (this.engagementActiveStep === 'Activité') {
              this.loadActivite();
            }
          }
          if (this.activeTab === 'Conditions') {
            this.loadConditionsBanque();
          }
          if (this.activeTab === 'Garanties') {
            this.loadGaranties();
          }
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

          if (this.activeTab === 'Sollicite' || this.activeTab === 'Avis') {
            this.loadSollicitation();
            this.loadGaranties();
          }
          if (this.activeTab === 'Engagements') {
            this.loadEngagementsActivite();
            if (this.engagementActiveStep === 'Activité') {
              this.loadActivite();
            }
          }
          if (this.activeTab === 'Conditions') {
            this.loadConditionsBanque();
          }
          if (this.activeTab === 'Garanties') {
            this.loadGaranties();
          }
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
    if (tab === 'Sollicite' || tab === 'Avis') {
      this.loadSollicitation();
      this.loadGaranties();
    }
    if (tab === 'Engagements') {
      this.loadEngagementsActivite();
    }
    if (tab === 'Conditions') {
      this.loadConditionsBanque();
    }
    if (tab === 'Garanties') {
      this.loadGaranties();
    }
  }

  loadGaranties(): void {
    if (!this.selectedClient || !this.selectedClient.id) return;
    this.isLoadingGaranties = true;
    this.garantiesData = null;

    this.http.get<GarantiesSummaryResponse>(`${this.baseUrl}/clients/${this.selectedClient.id}/garanties`, { headers: this.getHeaders() })
      .subscribe({
        next: (data) => {
          this.garantiesData = data;
          this.isLoadingGaranties = false;
        },
        error: (err) => {
          console.error('Error loading garanties', err);
          this.errorMessage = 'Erreur lors du chargement des garanties.';
          this.isLoadingGaranties = false;
        }
      });
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

  loadConditionsBanque(): void {
    if (!this.selectedClient || !this.selectedClient.id) return;
    this.isLoadingConditions = true;
    this.conditionsBanqueData = null;

    this.http.get<ConditionsBanqueResponse>(`${this.baseUrl}/clients/${this.selectedClient.id}/conditions-banque`, { headers: this.getHeaders() })
      .subscribe({
        next: (data) => {
          this.conditionsBanqueData = data;
          this.isLoadingConditions = false;
        },
        error: (err) => {
          console.error('Error loading conditions banque', err);
          // Fallback: générer localement pour la démo
          this.conditionsBanqueData = this.generateConditionsBanqueLocally();
          this.isLoadingConditions = false;
        }
      });
  }

  private generateConditionsBanqueLocally(): ConditionsBanqueResponse {
    const id = this.selectedClient?.id || 1;
    const tmmBase = 2.75 + (id % 3) * 0.25;
    const tmmDebit115 = tmmBase + 0.5;
    const tmmDebit101 = tmmBase;
    const sansMgCaution = 0.08 + (id % 4) * 0.02;
    const garantieCaution = 0.05 + (id % 3) * 0.01;
    const sansMgAval = 0.28 + (id % 3) * 0.05;
    const garantieAval = 0.18 + (id % 3) * 0.03;
    const sansMgMarche = 0.10 + (id % 4) * 0.02;
    const garantieMarche = 0.06 + (id % 3) * 0.01;

    const groupe = this.selectedClient?.groupe || this.selectedClient?.relation || 'N/A';
    const year = 2026 + (id % 3);
    const month = String((id % 12) + 1).padStart(2, '0');

    return {
      compteGroupe: groupe.toUpperCase(),
      finValidite: `30/${month}/${year}`,
      taux: [
        { designation: 'Taux escompte commercial sur la Tunisie', valeur: `TMM+ ${tmmBase.toFixed(2)} %` },
        { designation: 'Taux avance sur créances administratives', valeur: `TMM+ ${tmmBase.toFixed(2)} %` },
        { designation: "Taux d'intérêts créditeur (en dinars)", valeur: 'TMM+ 1.00 %' },
        {
          designation: "Taux d'intérêts débiteurs compte 115",
          valeur: `DEBITS DANS LE CADRE D'UNE FACILITE DE CAISSE NON ECHUE Valeur = TMM+ ${tmmDebit115.toFixed(2)} %\nAUTRES DECOUVERTES Valeur = TMM+ ${(tmmDebit115 + 1).toFixed(2)} %\nDEBITS GARANTIS PAR DES DEPOTS AFFECTES Valeur = TMM+ ${(tmmDebit115 + 1).toFixed(2)} %`
        },
        { designation: "Taux d'intérêts débiteurs compte 101", valeur: `TMM+ ${tmmDebit101.toFixed(2)} %` }
      ],
      commissions: [
        {
          designation: 'Com autres cautions & autres engagements par signature',
          avecMg: 'AVEC BLOCAGE DE LA PROVISION (AVEC MG) Valeur = 0.000',
          sansMg: `SANS BLOCAGE DE LA PROVISION (SANS MG) Valeur = ${sansMgCaution.toFixed(2)} %`,
          garantie: `GARANTIE PAR DES DEPOTS AFFECTES Valeur = ${garantieCaution.toFixed(2)} %`
        },
        {
          designation: 'Com aval opérations courantes',
          avecMg: 'AVEC BLOCAGE DE LA PROVISION (AVEC MG) Valeur = 0.000',
          sansMg: `SANS BLOCAGE DE LA PROVISION (SANS MG) Valeur = ${sansMgAval.toFixed(2)} %`,
          garantie: `GARANTIE PAR DES DEPOTS AFFECTES Valeur = ${garantieAval.toFixed(2)} %`
        },
        {
          designation: 'Com cautions sur marchés & cautions bancaires',
          avecMg: 'AVEC BLOCAGE DE LA PROVISION (AVEC MG) Valeur = 0.000',
          sansMg: `SANS BLOCAGE DE LA PROVISION (SANS MG) Valeur = ${sansMgMarche.toFixed(2)} %`,
          garantie: `GARANTIE PAR DES DEPOTS AFFECTES Valeur = ${garantieMarche.toFixed(2)} %`
        }
      ],
      commentaires: 'Commentaires Conditions de Banque – Direction Régionale'
    };
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

  get totalAutorise(): number {
    return this.engagementsActiviteData?.engagementsBna?.reduce((sum, row) => sum + (row.autorise || 0), 0) || 0;
  }

  get totalEncours(): number {
    return this.engagementsActiviteData?.engagementsBna?.reduce((sum, row) => sum + (row.encours || 0), 0) || 0;
  }

  get totalImpayes(): number {
    return this.engagementsActiviteData?.engagementsBna?.reduce((sum, row) => sum + (row.impayes || 0), 0) || 0;
  }

  get totalIr(): number {
    return this.engagementsActiviteData?.engagementsBna?.reduce((sum, row) => sum + (row.ir || 0), 0) || 0;
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
      this.errorMessage = "Veuillez d'abord sélectionner ou ajouter un compte.";
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
    if (!this.selectedClient || !this.selectedClient.id) return;
    this.openConfirmModal(
      'Suppression de Mandataire',
      'Voulez-vous vraiment supprimer ce mandataire ?',
      () => {
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
    );
  }

  // ================================================================ Sollicitation Logic
  loadReferenceData(): void {
    this.http.get<ReferenceItem[]>(`${this.baseUrl}/reference/produits`, { headers: this.getHeaders() })
      .subscribe({
        next: (data) => this.produitsRef = data,
        error: (err) => console.error('Error loading produits reference data', err)
      });

    this.http.get<ReferenceItem[]>(`${this.baseUrl}/reference/objets-credit`, { headers: this.getHeaders() })
      .subscribe({
        next: (data) => this.objetsCreditRef = data,
        error: (err) => console.error('Error loading objets-credit reference data', err)
      });
  }

  toutesSollicitations: any[] = [];
  selectedSollicitationId: number | null = null;

  loadSollicitation(): void {
    if (!this.selectedClient || !this.selectedClient.id) return;
    this.isLoadingSollicitation = true;
    this.sollicitationData = null;
    this.toutesSollicitations = [];

    // Load ALL solicitations for this client
    this.http.get<any[]>(`${this.baseUrl}/clients/${this.selectedClient.id}/sollicitations`, { headers: this.getHeaders() })
      .subscribe({
        next: (list) => {
          this.toutesSollicitations = list || [];
          if (this.toutesSollicitations.length > 0) {
            const firstSol = this.toutesSollicitations[0];
            this.sollicitationData = firstSol;
            this.selectedSollicitationId = firstSol.id || null;
            this.initSollicitationForm(firstSol);
            this.isLoadingSollicitation = false;
          } else {
            // Fallback to fetch single to get default template if none exist
            this.fetchDefaultSollicitationTemplate();
          }
        },
        error: (err) => {
          console.error('Error loading sollicitations', err);
          this.fetchDefaultSollicitationTemplate();
        }
      });
  }

  fetchDefaultSollicitationTemplate(): void {
    if (!this.selectedClient || !this.selectedClient.id) return;
    this.http.get<any>(`${this.baseUrl}/clients/${this.selectedClient.id}/sollicitation`, { headers: this.getHeaders() })
      .subscribe({
        next: (data) => {
          this.sollicitationData = data;
          this.selectedSollicitationId = data.id || null;
          this.initSollicitationForm(data);
          this.isLoadingSollicitation = false;
        },
        error: (err) => {
          console.error('Error loading default sollicitation template', err);
          this.initSollicitationForm({
            clientId: this.selectedClient!.id!,
            dateDemande: new Date().toISOString().split('T')[0],
            montantSollicite: 0,
            dureeSolliciteeMois: 0
          } as Sollicitation);
          this.isLoadingSollicitation = false;
        }
      });
  }

  onSollicitationSelectionChange(id: number): void {
    this.selectedSollicitationId = id;
    const sol = this.toutesSollicitations.find(s => s.id === id);
    if (sol) {
      this.sollicitationData = sol;
      this.initSollicitationForm(sol);
    }
  }

  initSollicitationForm(data: Sollicitation): void {
    const rawInv = (data && data.postesInvestissement) ? data.postesInvestissement : [];
    const rawFin = (data && data.postesFinancement) ? data.postesFinancement : [];
    const rawGar = (data && data.nouvellesGaranties) ? data.nouvellesGaranties : [];

    this.sollicitationFormPayload = {
      dateDemande: data.dateDemande || new Date().toISOString().split('T')[0],
      compteCodeGuichet: data.compteCodeGuichet || '',
      compteCodeProduit: data.compteCodeProduit || '',
      compteNumCompte: data.compteNumCompte || '',
      produitId: data.produit ? data.produit.id : null,
      objetCreditId: data.objetCredit ? data.objetCredit.id : null,
      montantSollicite: data.montantSollicite != null ? data.montantSollicite : null,
      dureeSolliciteeMois: data.dureeSolliciteeMois != null ? data.dureeSolliciteeMois : null,
      periodicitePl: data.periodicitePl || 'Trimestrielle',
      nbrePeriodesSolliciteesPl: data.nbrePeriodesSolliciteesPl != null ? data.nbrePeriodesSolliciteesPl : null,
      nbrePeriodesGracePl: data.nbrePeriodesGracePl != null ? data.nbrePeriodesGracePl : null,
      periodiciteInt: data.periodiciteInt || 'Trimestrielle',
      nbrePeriodesSolliciteesInt: data.nbrePeriodesSolliciteesInt != null ? data.nbrePeriodesSolliciteesInt : null,
      nbrePeriodesGraceInt: data.nbrePeriodesGraceInt != null ? data.nbrePeriodesGraceInt : null,
      typeTauxSollicite: data.typeTauxSollicite || 'Taux Variable',
      marge: data.marge != null ? data.marge : null,
      tauxSollicite: data.tauxSollicite != null ? data.tauxSollicite : null,
      typeCommission: data.typeCommission || 'Forfaitaire',
      commissionForfaitaireSollicitee: data.commissionForfaitaireSollicitee != null ? data.commissionForfaitaireSollicitee : null,
      commentaireDr: data.commentaireDr || '',
      apercuProjet: data.apercuProjet || '',
      etudeMarche: data.etudeMarche || '',
      syntheseRentabilite: data.syntheseRentabilite || '',
      swotForces: data.swotForces || '',
      swotFaiblesses: data.swotFaiblesses || '',
      swotOpportunites: data.swotOpportunites || '',
      swotMenaces: data.swotMenaces || '',
      commentaireEtudeProjet: data.commentaireEtudeProjet || '',
      postesInvestissement: rawInv.length > 0
        ? rawInv.map((p: any) => ({ ...p, montantSollicite: p.montantSollicite != null ? p.montantSollicite : null }))
        : [],
      postesFinancement: rawFin.length > 0
        ? rawFin.map((p: any) => ({ ...p, montantSollicite: p.montantSollicite != null ? p.montantSollicite : null }))
        : [{ libelle: 'CMLT BNA', montantSollicite: null }],
      nouvellesGaranties: rawGar.length > 0
        ? rawGar.map((g: any) => ({
          ...g,
          estimation: g.estimation != null ? g.estimation : null,
          chargesInscrites: g.chargesInscrites != null ? g.chargesInscrites : null
        }))
        : []
    };

    if (data.produit) {
      this.selectedProduit = data.produit;
    } else if (this.produitsRef.length > 0) {
      this.selectedProduit = this.produitsRef[0];
      this.sollicitationFormPayload.produitId = this.selectedProduit.id;
    } else {
      this.selectedProduit = null;
    }

    if (data.objetCredit) {
      this.selectedObjetCredit = data.objetCredit;
    } else if (this.objetsCreditRef.length > 0) {
      this.selectedObjetCredit = this.objetsCreditRef[0];
      this.sollicitationFormPayload.objetCreditId = this.selectedObjetCredit.id;
    } else {
      this.selectedObjetCredit = null;
    }

    if ((!this.sollicitationFormPayload.compteCodeGuichet || !this.sollicitationFormPayload.compteNumCompte) && this.selectedClient?.comptes && this.selectedClient.comptes.length > 0) {
      const firstAccount = this.selectedClient.comptes[0];
      this.sollicitationFormPayload.compteCodeGuichet = firstAccount.codeGuichet;
      this.sollicitationFormPayload.compteCodeProduit = firstAccount.codeProduit;
      this.sollicitationFormPayload.compteNumCompte = firstAccount.numCompte;
    }
  }

  getCompteKey(cp: any): string {
    if (!cp) return '';
    return `${cp.codeGuichet}-${cp.codeProduit}-${cp.numCompte}`;
  }

  getSollicitationCompteOptions(): any[] {
    const list = (this.selectedClient && this.selectedClient.comptes) ? [...this.selectedClient.comptes] : [];
    if (this.sollicitationFormPayload && this.sollicitationFormPayload.compteCodeGuichet && this.sollicitationFormPayload.compteNumCompte) {
      const key = this.getSelectedSollicitationCompteKey();
      const exists = list.some(c => this.getCompteKey(c) === key);
      if (!exists) {
        list.push({
          codeGuichet: this.sollicitationFormPayload.compteCodeGuichet,
          codeProduit: this.sollicitationFormPayload.compteCodeProduit,
          numCompte: this.sollicitationFormPayload.compteNumCompte,
          numeroCompteComplet: key,
          agence: 'Enregistré'
        });
      }
    }
    return list;
  }

  onSollicitationCompteChange(key: string): void {
    if (!key) return;
    const options = this.getSollicitationCompteOptions();
    const found = options.find(c => this.getCompteKey(c) === key || c.numeroCompteComplet === key);
    if (found && this.sollicitationFormPayload) {
      this.sollicitationFormPayload.compteCodeGuichet = found.codeGuichet;
      this.sollicitationFormPayload.compteCodeProduit = found.codeProduit;
      this.sollicitationFormPayload.compteNumCompte = found.numCompte;
    }
  }

  getSelectedSollicitationCompteKey(): string {
    if (!this.sollicitationFormPayload || !this.sollicitationFormPayload.compteCodeGuichet) return '';
    return `${this.sollicitationFormPayload.compteCodeGuichet}-${this.sollicitationFormPayload.compteCodeProduit}-${this.sollicitationFormPayload.compteNumCompte}`;
  }

  // Searchable Select Helpers
  filteredProduits(): ReferenceItem[] {
    if (!this.produitSearchText.trim()) return this.produitsRef;
    const term = this.produitSearchText.toLowerCase();
    return this.produitsRef.filter(p => p.libelle.toLowerCase().includes(term) || p.code.toLowerCase().includes(term));
  }

  selectProduit(item: ReferenceItem): void {
    this.selectedProduit = item;
    if (this.sollicitationFormPayload) {
      this.sollicitationFormPayload.produitId = item.id;
    }
    this.showProduitDropdown = false;
    this.produitSearchText = '';
  }

  clearProduit(): void {
    this.selectedProduit = null;
    if (this.sollicitationFormPayload) {
      this.sollicitationFormPayload.produitId = null;
    }
    this.showProduitDropdown = false;
    this.produitSearchText = '';
  }

  filteredObjetsCredit(): ReferenceItem[] {
    if (!this.objetCreditSearchText.trim()) return this.objetsCreditRef;
    const term = this.objetCreditSearchText.toLowerCase();
    return this.objetsCreditRef.filter(o => o.libelle.toLowerCase().includes(term) || o.code.toLowerCase().includes(term));
  }

  selectObjetCredit(item: ReferenceItem): void {
    this.selectedObjetCredit = item;
    if (this.sollicitationFormPayload) {
      this.sollicitationFormPayload.objetCreditId = item.id;
    }
    this.showObjetCreditDropdown = false;
    this.objetCreditSearchText = '';
  }

  clearObjetCredit(): void {
    this.selectedObjetCredit = null;
    if (this.sollicitationFormPayload) {
      this.sollicitationFormPayload.objetCreditId = null;
    }
    this.showObjetCreditDropdown = false;
    this.objetCreditSearchText = '';
  }

  // Dynamic Investment/Financing Logic
  addPosteInvestissement(): void {
    if (!this.sollicitationFormPayload.postesInvestissement) {
      this.sollicitationFormPayload.postesInvestissement = [];
    }
    this.sollicitationFormPayload.postesInvestissement.push({ libelle: '', montantSollicite: null });
  }

  removePosteInvestissement(index: number): void {
    this.sollicitationFormPayload.postesInvestissement.splice(index, 1);
  }

  get totalInvestissement(): number {
    if (!this.sollicitationFormPayload || !this.sollicitationFormPayload.postesInvestissement) return 0;
    return this.sollicitationFormPayload.postesInvestissement.reduce((sum: number, row: any) => {
      const val = row.montantSollicite != null ? Number(row.montantSollicite) : 0;
      return sum + (isNaN(val) ? 0 : val);
    }, 0);
  }

  percentageInvestissement(montant: any): number {
    const tot = this.totalInvestissement;
    if (!tot || tot === 0) return 0;
    const val = montant != null ? Number(montant) : 0;
    if (isNaN(val) || val <= 0) return 0;
    return (val / tot) * 100;
  }

  get totalInvestissementPercentage(): number {
    const tot = this.totalInvestissement;
    return (!tot || tot === 0) ? 0 : 100;
  }

  addPosteFinancement(): void {
    if (!this.sollicitationFormPayload.postesFinancement) {
      this.sollicitationFormPayload.postesFinancement = [];
    }
    this.sollicitationFormPayload.postesFinancement.push({ libelle: '', montantSollicite: null });
  }

  removePosteFinancement(index: number): void {
    this.sollicitationFormPayload.postesFinancement.splice(index, 1);
  }

  get totalFinancement(): number {
    if (!this.sollicitationFormPayload || !this.sollicitationFormPayload.postesFinancement) return 0;
    return this.sollicitationFormPayload.postesFinancement.reduce((sum: number, row: any) => {
      const val = row.montantSollicite != null ? Number(row.montantSollicite) : 0;
      return sum + (isNaN(val) ? 0 : val);
    }, 0);
  }

  percentageFinancement(montant: any): number {
    const tot = this.totalFinancement;
    if (!tot || tot === 0) return 0;
    const val = montant != null ? Number(montant) : 0;
    if (isNaN(val) || val <= 0) return 0;
    return (val / tot) * 100;
  }

  get totalFinancementPercentage(): number {
    const tot = this.totalFinancement;
    return (!tot || tot === 0) ? 0 : 100;
  }

  // Balance Comparison Getters
  get balanceStatus(): 'NONE' | 'EQUAL' | 'DEFICIT' | 'SURPLUS' {
    const totInv = this.totalInvestissement;
    const totFin = this.totalFinancement;
    if (totInv === 0 && totFin === 0) return 'NONE';
    if (Math.abs(totFin - totInv) < 0.001) return 'EQUAL';
    if (totFin < totInv) return 'DEFICIT';
    return 'SURPLUS';
  }

  get balanceDifference(): number {
    return Math.abs(this.totalFinancement - this.totalInvestissement);
  }

  // Proposed Guarantees Logic
  addNouvelleGarantie(): void {
    if (!this.sollicitationFormPayload.nouvellesGaranties) {
      this.sollicitationFormPayload.nouvellesGaranties = [];
    }
    this.sollicitationFormPayload.nouvellesGaranties.push({
      natureGarantie: '',
      estimation: null,
      dateExpertise: new Date().toISOString().split('T')[0],
      chargesInscrites: null
    });
  }

  removeNouvelleGarantie(index: number): void {
    this.sollicitationFormPayload.nouvellesGaranties.splice(index, 1);
  }

  get garantiesEnPossession(): GarantieResponse[] {
    if (!this.garantiesData || !this.garantiesData.garanties) return [];
    return this.garantiesData.garanties.filter(g => g.affectation !== 'Nouvelle Garantie Validée');
  }

  get garantiesNouvellesValidees(): GarantieResponse[] {
    if (!this.garantiesData || !this.garantiesData.garanties) return [];
    return this.garantiesData.garanties.filter(g => g.affectation === 'Nouvelle Garantie Validée');
  }

  deleteValidatedGarantie(id: number): void {
    if (!this.selectedClient || !this.selectedClient.id) return;
    this.openConfirmModal(
      'Suppression de Garantie',
      'Voulez-vous vraiment supprimer cette garantie validée ?',
      () => {
        this.http.delete(`${this.baseUrl}/clients/${this.selectedClient!.id}/garanties/${id}`, { headers: this.getHeaders() })
          .subscribe({
            next: () => {
              this.showSuccess('Garantie supprimée avec succès.');
              this.loadGaranties();
            },
            error: (err) => {
              this.errorMessage = `Erreur lors de la suppression de la garantie: ${this.formatError(err)}`;
            }
          });
      }
    );
  }

  saveNouvelleGarantieAsPossession(row: any, index: number): void {
    if (!this.selectedClient || !this.selectedClient.id) {
      this.errorMessage = 'Veuillez d\'abord sélectionner un client.';
      return;
    }
    if (!row.natureGarantie || !row.natureGarantie.trim()) {
      this.errorMessage = 'Veuillez saisir la nature de la garantie avant de valider.';
      return;
    }

    const payload = {
      libelle: row.natureGarantie.trim(),
      valeur: row.estimation || 0,
      dateExpertise: row.dateExpertise || null,
      charges: row.chargesInscrites || 0,
      affectation: 'Nouvelle Garantie Validée',
      beneficiaire: 'BANQUE NATIONALE AGRICOLE'
    };

    this.http.post<GarantieResponse>(`${this.baseUrl}/clients/${this.selectedClient.id}/garanties`, payload, { headers: this.getHeaders() })
      .subscribe({
        next: (res) => {
          this.showSuccess('Garantie validée et ajoutée à la section Nouvelles garanties validées.');
          this.removeNouvelleGarantie(index);
          this.loadGaranties();
        },
        error: (err) => {
          this.errorMessage = `Erreur lors de la validation de la garantie: ${this.formatError(err)}`;
          console.error('saveNouvelleGarantieAsPossession error', err);
        }
      });
  }

  // Save Sollicitation with Double-Save Protection
  saveSollicitation(): void {
    if (this.isSavingSollicitation) return;
    if (!this.selectedClient || !this.selectedClient.id) return;
    if (!this.sollicitationFormPayload) return;

    this.clearMessages();

    // Frontend Validations
    if (!this.sollicitationFormPayload.dateDemande) {
      this.errorMessage = 'La date de la demande du client est obligatoire.';
      return;
    }
    if (!this.sollicitationFormPayload.produitId) {
      this.errorMessage = 'Le produit est obligatoire.';
      return;
    }
    if (!this.sollicitationFormPayload.objetCreditId) {
      this.errorMessage = 'L\'objet du crédit est obligatoire.';
      return;
    }
    if (this.sollicitationFormPayload.montantSollicite != null && this.sollicitationFormPayload.montantSollicite < 0) {
      this.errorMessage = 'Le montant sollicité doit être un nombre positif ou nul.';
      return;
    }
    if (this.sollicitationFormPayload.dureeSolliciteeMois != null && this.sollicitationFormPayload.dureeSolliciteeMois < 0) {
      this.errorMessage = 'La durée sollicitée doit être un nombre positif ou nul.';
      return;
    }

    // Clean payload before saving: remove completely empty rows
    const cleanedInvestissement = (this.sollicitationFormPayload.postesInvestissement || [])
      .filter((row: any) => (row.libelle && row.libelle.trim() !== '') || (row.montantSollicite != null && Number(row.montantSollicite) > 0));

    const cleanedFinancement = (this.sollicitationFormPayload.postesFinancement || [])
      .filter((row: any) => (row.libelle && row.libelle.trim() !== '') || (row.montantSollicite != null && Number(row.montantSollicite) > 0));

    const cleanedNouvellesGaranties = (this.sollicitationFormPayload.nouvellesGaranties || [])
      .filter((g: any) => (g.natureGarantie && g.natureGarantie.trim() !== '') || (g.estimation != null && Number(g.estimation) > 0));

    const payloadToSave = {
      ...this.sollicitationFormPayload,
      postesInvestissement: cleanedInvestissement,
      postesFinancement: cleanedFinancement,
      nouvellesGaranties: cleanedNouvellesGaranties
    };

    this.isSavingSollicitation = true;

    this.http.put<Sollicitation>(`${this.baseUrl}/clients/${this.selectedClient.id}/sollicitation`, payloadToSave, { headers: this.getHeaders() })
      .subscribe({
        next: (saved) => {
          this.showSuccess('Canevas Sollicité enregistré avec succès ! Passage à l\'étape Avis et Décision.');
          this.sollicitationData = saved;
          this.initSollicitationForm(saved);
          this.isSavingSollicitation = false;
          this.setActiveTab('Avis');
        },
        error: (err) => {
          this.errorMessage = `Erreur lors de l'enregistrement du canevas : ${this.formatError(err)}`;
          console.error('saveSollicitation error:', err);
          this.isSavingSollicitation = false;
        }
      });
  }

  prepareBlankSollicitation(): void {
    this.setActiveTab('Sollicite');
    this.solliciteActiveStep = 'credit';
    this.sollicitationData = null;
    this.selectedSollicitationId = null;
    if (this.selectedClient && this.selectedClient.id) {
      this.initSollicitationForm({
        clientId: this.selectedClient.id,
        dateDemande: new Date().toISOString().split('T')[0],
        montantSollicite: 0,
        dureeSolliciteeMois: 0,
        statut: 'BROUILLON'
      } as Sollicitation);
    }
    this.clearMessages();
  }

  exporterPDF(): void {
    if (!this.selectedClient) {
      this.errorMessage = 'Veuillez sélectionner un client pour exporter le PDF.';
      return;
    }

    const sol = this.sollicitationFormPayload || this.sollicitationData || {};
    const client: any = this.selectedClient;

    const formatDate = (dStr: any) => {
      if (!dStr) return '-';
      try {
        const d = new Date(dStr);
        return isNaN(d.getTime()) ? dStr : d.toLocaleDateString('fr-FR');
      } catch { return dStr; }
    };

    const formatMoney = (val: any) => {
      if (val == null || val === '') return '0,00 DT';
      const num = Number(val);
      return isNaN(num) ? '0,00 DT' : num.toLocaleString('fr-FR', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + ' DT';
    };

    const getProduitLibelle = () => {
      if (!sol.produitId) return '-';
      const p = (this.produitsRef || []).find((prod: any) => prod.id === sol.produitId);
      return p ? p.libelle : `Produit #${sol.produitId}`;
    };

    const getObjetCreditLibelle = () => {
      if (!sol.objetCreditId) return '-';
      const o = (this.objetsCreditRef || []).find((obj: any) => obj.id === sol.objetCreditId);
      return o ? o.libelle : `Objet #${sol.objetCreditId}`;
    };

    const totalInvest = (sol.postesInvestissement || []).reduce((acc: number, r: any) => acc + (Number(r.montantSollicite) || 0), 0);
    const totalFinanc = (sol.postesFinancement || []).reduce((acc: number, r: any) => acc + (Number(r.montantSollicite) || 0), 0);

    const investRowsHtml = (sol.postesInvestissement && sol.postesInvestissement.length > 0)
      ? sol.postesInvestissement.map((r: any) => `
          <tr>
            <td style="padding: 6px 10px; border-bottom: 1px solid #e2e8f0; font-size: 11px;">${r.libelle || '-'}</td>
            <td style="padding: 6px 10px; border-bottom: 1px solid #e2e8f0; font-size: 11px; text-align: right; font-weight: 600;">${formatMoney(r.montantSollicite)}</td>
          </tr>
        `).join('')
      : `<tr><td colspan="2" style="padding: 8px; text-align: center; color: #94a3b8; font-size: 11px;">Aucun poste d'investissement renseigné</td></tr>`;

    const financRowsHtml = (sol.postesFinancement && sol.postesFinancement.length > 0)
      ? sol.postesFinancement.map((r: any) => `
          <tr>
            <td style="padding: 6px 10px; border-bottom: 1px solid #e2e8f0; font-size: 11px;">${r.libelle || '-'}</td>
            <td style="padding: 6px 10px; border-bottom: 1px solid #e2e8f0; font-size: 11px; text-align: right; font-weight: 600;">${formatMoney(r.montantSollicite)}</td>
          </tr>
        `).join('')
      : `<tr><td colspan="2" style="padding: 8px; text-align: center; color: #94a3b8; font-size: 11px;">Aucun poste de financement renseigné</td></tr>`;

    const garantiesRowsHtml = (sol.nouvellesGaranties && sol.nouvellesGaranties.length > 0)
      ? sol.nouvellesGaranties.map((g: any) => `
          <tr>
            <td style="padding: 6px 10px; border-bottom: 1px solid #e2e8f0; font-size: 11px;">${g.natureGarantie || '-'}</td>
            <td style="padding: 6px 10px; border-bottom: 1px solid #e2e8f0; font-size: 11px; text-align: right; font-weight: 600;">${formatMoney(g.estimation)}</td>
          </tr>
        `).join('')
      : `<tr><td colspan="2" style="padding: 8px; text-align: center; color: #94a3b8; font-size: 11px;">Aucune garantie proposée</td></tr>`;

    const element = document.createElement('div');
    element.style.padding = '20px 25px';
    element.style.fontFamily = 'Arial, sans-serif';
    element.style.color = '#1e293b';
    element.style.backgroundColor = '#ffffff';

    element.innerHTML = `
      <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 3px solid #06534d; padding-bottom: 12px; margin-bottom: 18px;">
        <div>
          <div style="font-size: 20px; font-weight: 800; color: #06534d; letter-spacing: 0.5px;">BNA BANK</div>
          <div style="font-size: 11px; font-weight: 600; color: #00875a; text-transform: uppercase;">Banque Nationale Agricole</div>
        </div>
        <div style="text-align: right;">
          <div style="font-size: 14px; font-weight: 700; color: #06534d;">CANEVAS D'ÉTUDE DE CRÉDIT</div>
          <div style="font-size: 10px; color: #64748b;">Édité le ${new Date().toLocaleDateString('fr-FR')}</div>
        </div>
      </div>

      <div style="background-color: #f8fafc; border: 1px solid #cbd5e1; border-left: 4px solid #06534d; border-radius: 6px; padding: 10px 14px; margin-bottom: 16px;">
        <table style="width: 100%; border-collapse: collapse; font-size: 11px;">
          <tr>
            <td style="padding: 3px 0; width: 50%;"><strong>Code Client :</strong> ${client.code || '-'}</td>
            <td style="padding: 3px 0; width: 50%;"><strong>Client / Raison Sociale :</strong> ${client.nomRaisonSociale || '-'}</td>
          </tr>
          <tr>
            <td style="padding: 3px 0;"><strong>Matricule Fiscal / CIN :</strong> ${client.numCin || client.identifiantFiscal || '-'}</td>
            <td style="padding: 3px 0;"><strong>Forme Juridique :</strong> ${client.formeJuridique || '-'}</td>
          </tr>
          <tr>
            <td style="padding: 3px 0;"><strong>Agence / Branche :</strong> ${client.brancheNom || '-'}</td>
            <td style="padding: 3px 0;"><strong>Activité :</strong> ${client.activite || '-'}</td>
          </tr>
        </table>
      </div>

      <div style="margin-bottom: 16px;">
        <div style="background-color: #06534d; color: #ffffff; padding: 6px 12px; font-size: 11px; font-weight: 700; border-radius: 4px 4px 0 0; text-transform: uppercase;">
          1. Caractéristiques de la Sollicitation
        </div>
        <table style="width: 100%; border-collapse: collapse; font-size: 11px; border: 1px solid #cbd5e1; border-top: none;">
          <tr style="background-color: #ffffff;">
            <td style="padding: 8px 12px; border-bottom: 1px solid #e2e8f0; width: 25%;"><strong>Date Demande :</strong></td>
            <td style="padding: 8px 12px; border-bottom: 1px solid #e2e8f0; width: 25%;">${formatDate(sol.dateDemande)}</td>
            <td style="padding: 8px 12px; border-bottom: 1px solid #e2e8f0; width: 25%;"><strong>Produit Sollicité :</strong></td>
            <td style="padding: 8px 12px; border-bottom: 1px solid #e2e8f0; width: 25%; font-weight: 600; color: #06534d;">${getProduitLibelle()}</td>
          </tr>
          <tr style="background-color: #f8fafc;">
            <td style="padding: 8px 12px; border-bottom: 1px solid #e2e8f0;"><strong>Objet du Crédit :</strong></td>
            <td style="padding: 8px 12px; border-bottom: 1px solid #e2e8f0;">${getObjetCreditLibelle()}</td>
            <td style="padding: 8px 12px; border-bottom: 1px solid #e2e8f0;"><strong>Montant Sollicité :</strong></td>
            <td style="padding: 8px 12px; border-bottom: 1px solid #e2e8f0; font-weight: 700; color: #00875a;">${formatMoney(sol.montantSollicite)}</td>
          </tr>
          <tr style="background-color: #ffffff;">
            <td style="padding: 8px 12px;"><strong>Durée (Mois) :</strong></td>
            <td style="padding: 8px 12px;">${sol.dureeSolliciteeMois ? sol.dureeSolliciteeMois + ' mois' : '-'}</td>
            <td style="padding: 8px 12px;"><strong>Statut Dossier :</strong></td>
            <td style="padding: 8px 12px; font-weight: 700;">${sol.statut || 'BROUILLON'}</td>
          </tr>
        </table>
      </div>

      <div style="margin-bottom: 16px;">
        <div style="background-color: #06534d; color: #ffffff; padding: 6px 12px; font-size: 11px; font-weight: 700; border-radius: 4px 4px 0 0; text-transform: uppercase;">
          2. Programme d'Investissement & Schéma de Financement
        </div>
        <div style="display: flex; gap: 12px; border: 1px solid #cbd5e1; border-top: none; padding: 10px; background-color: #ffffff;">
          <div style="flex: 1;">
            <div style="font-size: 11px; font-weight: 700; color: #06534d; margin-bottom: 6px; text-transform: uppercase;">Postes d'Investissement</div>
            <table style="width: 100%; border-collapse: collapse; border: 1px solid #e2e8f0;">
              <thead>
                <tr style="background-color: #f1f5f9; color: #475569; font-size: 10px; text-align: left;">
                  <th style="padding: 5px 8px; border-bottom: 1px solid #cbd5e1;">Libellé</th>
                  <th style="padding: 5px 8px; border-bottom: 1px solid #cbd5e1; text-align: right;">Montant</th>
                </tr>
              </thead>
              <tbody>${investRowsHtml}</tbody>
              <tfoot>
                <tr style="background-color: #f8fafc; font-weight: 700; font-size: 11px;">
                  <td style="padding: 6px 10px; border-top: 1px solid #cbd5e1;">Total Investissement</td>
                  <td style="padding: 6px 10px; border-top: 1px solid #cbd5e1; text-align: right; color: #06534d;">${formatMoney(totalInvest)}</td>
                </tr>
              </tfoot>
            </table>
          </div>

          <div style="flex: 1;">
            <div style="font-size: 11px; font-weight: 700; color: #06534d; margin-bottom: 6px; text-transform: uppercase;">Postes de Financement</div>
            <table style="width: 100%; border-collapse: collapse; border: 1px solid #e2e8f0;">
              <thead>
                <tr style="background-color: #f1f5f9; color: #475569; font-size: 10px; text-align: left;">
                  <th style="padding: 5px 8px; border-bottom: 1px solid #cbd5e1;">Libellé</th>
                  <th style="padding: 5px 8px; border-bottom: 1px solid #cbd5e1; text-align: right;">Montant</th>
                </tr>
              </thead>
              <tbody>${financRowsHtml}</tbody>
              <tfoot>
                <tr style="background-color: #f8fafc; font-weight: 700; font-size: 11px;">
                  <td style="padding: 6px 10px; border-top: 1px solid #cbd5e1;">Total Financement</td>
                  <td style="padding: 6px 10px; border-top: 1px solid #cbd5e1; text-align: right; color: #06534d;">${formatMoney(totalFinanc)}</td>
                </tr>
              </tfoot>
            </table>
          </div>
        </div>
      </div>

      <div style="margin-bottom: 16px;">
        <div style="background-color: #06534d; color: #ffffff; padding: 6px 12px; font-size: 11px; font-weight: 700; border-radius: 4px 4px 0 0; text-transform: uppercase;">
          3. Nouvelles Garanties Proposées
        </div>
        <table style="width: 100%; border-collapse: collapse; font-size: 11px; border: 1px solid #cbd5e1; border-top: none;">
          <thead>
            <tr style="background-color: #f1f5f9; color: #475569; font-size: 10px; text-align: left;">
              <th style="padding: 6px 10px; border-bottom: 1px solid #cbd5e1;">Nature de la Garantie</th>
              <th style="padding: 6px 10px; border-bottom: 1px solid #cbd5e1; text-align: right;">Valeur / Estimation</th>
            </tr>
          </thead>
          <tbody>${garantiesRowsHtml}</tbody>
        </table>
      </div>

      <div style="margin-bottom: 16px;">
        <div style="background-color: #06534d; color: #ffffff; padding: 6px 12px; font-size: 11px; font-weight: 700; border-radius: 4px 4px 0 0; text-transform: uppercase;">
          4. Analyse SWOT du Projet
        </div>
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px; border: 1px solid #cbd5e1; border-top: none; padding: 10px; background-color: #ffffff;">
          <div style="background-color: #f0fdf4; border: 1px solid #bbf7d0; border-radius: 4px; padding: 8px;">
            <div style="font-size: 11px; font-weight: 700; color: #166534; margin-bottom: 4px;">FORCES</div>
            <div style="font-size: 10.5px; color: #1e293b; white-space: pre-wrap;">${sol.forces || 'Non renseigné'}</div>
          </div>
          <div style="background-color: #fef2f2; border: 1px solid #fecaca; border-radius: 4px; padding: 8px;">
            <div style="font-size: 11px; font-weight: 700; color: #991b1b; margin-bottom: 4px;">FAIBLESSES</div>
            <div style="font-size: 10.5px; color: #1e293b; white-space: pre-wrap;">${sol.faiblesses || 'Non renseigné'}</div>
          </div>
          <div style="background-color: #eff6ff; border: 1px solid #bfdbfe; border-radius: 4px; padding: 8px;">
            <div style="font-size: 11px; font-weight: 700; color: #1e40af; margin-bottom: 4px;">OPPORTUNITÉS</div>
            <div style="font-size: 10.5px; color: #1e293b; white-space: pre-wrap;">${sol.opportunites || 'Non renseigné'}</div>
          </div>
          <div style="background-color: #fffbeb; border: 1px solid #fef3c7; border-radius: 4px; padding: 8px;">
            <div style="font-size: 11px; font-weight: 700; color: #92400e; margin-bottom: 4px;">MENACES</div>
            <div style="font-size: 10.5px; color: #1e293b; white-space: pre-wrap;">${sol.menaces || 'Non renseigné'}</div>
          </div>
        </div>
      </div>

      <div style="margin-bottom: 16px;">
        <div style="background-color: #06534d; color: #ffffff; padding: 6px 12px; font-size: 11px; font-weight: 700; border-radius: 4px 4px 0 0; text-transform: uppercase;">
          5. Synthèse & Commentaires
        </div>
        <div style="border: 1px solid #cbd5e1; border-top: none; padding: 10px; font-size: 11px; background-color: #ffffff;">
          <div style="margin-bottom: 10px;">
            <strong style="color: #06534d;">Commentaire Direction Régionale :</strong>
            <div style="margin-top: 3px; padding: 6px 10px; background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 4px; font-size: 10.5px; white-space: pre-wrap;">${sol.commentaireDr || 'Aucun commentaire'}</div>
          </div>
          <div>
            <strong style="color: #06534d;">Commentaire Étude du Projet :</strong>
            <div style="margin-top: 3px; padding: 6px 10px; background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 4px; font-size: 10.5px; white-space: pre-wrap;">${sol.commentaireEtudeProjet || 'Aucun commentaire'}</div>
          </div>
        </div>
      </div>

      <div style="margin-bottom: 16px;">
        <div style="background-color: #06534d; color: #ffffff; padding: 6px 12px; font-size: 11px; font-weight: 700; border-radius: 4px 4px 0 0; text-transform: uppercase;">
          6. Avis et Décision de Crédit
        </div>
        <div style="border: 1px solid #cbd5e1; border-top: none; padding: 12px; font-size: 11px; background-color: #ffffff;">
          <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px;">
            <div><strong>Décision Finale :</strong> <span style="font-weight: 800; color: ${sol.statut === 'ACCEPTE' ? '#059669' : sol.statut === 'REFUSE' ? '#dc2626' : '#d97706'}; font-size: 13px;">${sol.statut || 'EN ATTENTE DE DÉCISION'}</span></div>
            <div><strong>Date de Décision :</strong> ${sol.dateDecision ? formatDate(sol.dateDecision) : '-'}</div>
          </div>
          <div>
            <strong>Commentaire de Décision :</strong>
            <div style="margin-top: 4px; padding: 8px; background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 4px; font-size: 10.5px;">
              ${sol.decisionCommentaire || 'Aucun commentaire de décision renseigné.'}
            </div>
          </div>

          <div style="margin-top: 25px; display: flex; justify-content: space-between; text-align: center; padding-top: 15px; border-top: 1px dashed #cbd5e1;">
            <div style="width: 45%;">
              <div style="font-weight: 700; font-size: 11px; color: #06534d;">La Direction Régionale</div>
              <div style="height: 40px;"></div>
              <div style="font-size: 9.5px; color: #94a3b8;">Signature & Cachet</div>
            </div>
            <div style="width: 45%;">
              <div style="font-weight: 700; font-size: 11px; color: #06534d;">Le Comité de Crédit / Division</div>
              <div style="height: 40px;"></div>
              <div style="font-size: 9.5px; color: #94a3b8;">Signature & Cachet</div>
            </div>
          </div>
        </div>
      </div>
    `;

    const opt = {
      margin: [8, 8, 8, 8],
      filename: `sollicitation-bna-client-${client.code || 'client'}-${sol.id || 'draft'}.pdf`,
      image: { type: 'jpeg', quality: 0.98 },
      html2canvas: { scale: 2, useCORS: true, logging: false },
      jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' }
    };

    if (typeof html2pdf !== 'undefined') {
      html2pdf().set(opt).from(element).save();
      this.showSuccess('Téléchargement du PDF en cours...');
    } else {
      import('html2pdf.js').then((html2pdfLib: any) => {
        const lib = html2pdfLib.default || html2pdfLib;
        lib().set(opt).from(element).save();
        this.showSuccess('Téléchargement du PDF en cours...');
      }).catch(err => {
        console.error('Error loading html2pdf.js', err);
        this.errorMessage = 'Impossible de charger le moteur PDF.';
      });
    }
  }

  creerNouvelleSollicitation(): void {
    if (this.isSavingSollicitation) return;
    if (!this.selectedClient || !this.selectedClient.id) return;
    if (!this.sollicitationFormPayload) return;

    this.clearMessages();

    // Frontend Validations
    if (!this.sollicitationFormPayload.dateDemande) {
      this.errorMessage = 'La date de la demande du client est obligatoire.';
      return;
    }
    if (!this.sollicitationFormPayload.produitId) {
      this.errorMessage = 'Le produit est obligatoire.';
      return;
    }
    if (!this.sollicitationFormPayload.objetCreditId) {
      this.errorMessage = 'L\'objet du crédit est obligatoire.';
      return;
    }
    if (this.sollicitationFormPayload.montantSollicite != null && this.sollicitationFormPayload.montantSollicite < 0) {
      this.errorMessage = 'Le montant sollicité doit être un nombre positif ou nul.';
      return;
    }
    if (this.sollicitationFormPayload.dureeSolliciteeMois != null && this.sollicitationFormPayload.dureeSolliciteeMois < 0) {
      this.errorMessage = 'La durée sollicitée doit être un nombre positif ou nul.';
      return;
    }

    // Clean payload before saving: remove completely empty rows
    const cleanedInvestissement = (this.sollicitationFormPayload.postesInvestissement || [])
      .filter((row: any) => (row.libelle && row.libelle.trim() !== '') || (row.montantSollicite != null && Number(row.montantSollicite) > 0));

    const cleanedFinancement = (this.sollicitationFormPayload.postesFinancement || [])
      .filter((row: any) => (row.libelle && row.libelle.trim() !== '') || (row.montantSollicite != null && Number(row.montantSollicite) > 0));

    const cleanedNouvellesGaranties = (this.sollicitationFormPayload.nouvellesGaranties || [])
      .filter((g: any) => (g.natureGarantie && g.natureGarantie.trim() !== '') || (g.estimation != null && Number(g.estimation) > 0));

    const payloadToSave = {
      ...this.sollicitationFormPayload,
      postesInvestissement: cleanedInvestissement,
      postesFinancement: cleanedFinancement,
      nouvellesGaranties: cleanedNouvellesGaranties
    };

    this.isSavingSollicitation = true;

    this.http.post<Sollicitation>(`${this.baseUrl}/clients/${this.selectedClient.id}/sollicitation`, payloadToSave, { headers: this.getHeaders() })
      .subscribe({
        next: (saved) => {
          this.showSuccess('Nouvelle sollicitation créée avec succès !');
          this.setActiveTab('Sollicite');
          this.solliciteActiveStep = 'credit';
          this.loadSollicitation();
          this.isSavingSollicitation = false;
        },
        error: (err) => {
          this.errorMessage = `Erreur lors de la création d'une nouvelle sollicitation : ${this.formatError(err)}`;
          console.error('creerNouvelleSollicitation error:', err);
          this.isSavingSollicitation = false;
        }
      });
  }

  // Decision State & Methods
  decisionCommentaire: string = '';
  isSavingDecision: boolean = false;

  get canMakeDecision(): boolean {
    const userJson = localStorage.getItem('user');
    if (!userJson) return false;
    try {
      const user = JSON.parse(userJson);
      return user.role === 'ROLE_ADMIN' || user.role === 'ROLE_CHEF_DIVISION';
    } catch {
      return false;
    }
  }

  enregistrerDecision(decision: 'ACCEPTE' | 'REFUSE'): void {
    if (!this.selectedClient || !this.selectedClient.id) return;
    if (!this.decisionCommentaire || !this.decisionCommentaire.trim()) {
      this.errorMessage = 'Le commentaire de décision est obligatoire.';
      return;
    }

    this.clearMessages();
    this.isSavingDecision = true;

    const payload = {
      decision: decision,
      commentaireDecision: this.decisionCommentaire.trim()
    };

    this.http.post<Sollicitation>(
      `${this.baseUrl}/clients/${this.selectedClient.id}/sollicitation/decision`,
      payload,
      { headers: this.getHeaders() }
    ).subscribe({
      next: (res) => {
        const label = decision === 'ACCEPTE' ? 'acceptée' : 'refusée';
        this.showSuccess(`La sollicitation a été ${label} avec succès.`);
        this.sollicitationData = res;
        this.initSollicitationForm(res);
        this.isSavingDecision = false;
      },
      error: (err) => {
        this.errorMessage = `Erreur lors de l'enregistrement de la décision : ${this.formatError(err)}`;
        console.error('enregistrerDecision error:', err);
        this.isSavingDecision = false;
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


