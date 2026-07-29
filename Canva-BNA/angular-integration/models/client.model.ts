// client.model.ts
// Angular models matching the Spring Boot DTOs for the business module

// ============================================================
// Structure
// ============================================================

export interface StructureRequest {
  nom: string;
  code: string;
}

export interface StructureResponse {
  id: number;
  nom: string;
  code: string;
}

// ============================================================
// Client (fiche signalétique entreprise)
// ============================================================

export interface ClientRequest {
  identifiant: string;
  groupe: string;
  relation: string;
  activite?: string;
  segment?: string;
  siegeSocial?: string;
  secteur?: string;
  dateEntreeRelation?: string;   // ISO date string 'YYYY-MM-DD'
  formeJuridique?: string;
  capitalSocial?: number;
  agence?: string;
  directionRegionale?: string;
  structureId?: number;
}

export interface ClientResponse {
  id: number;
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
  structure?: StructureResponse;
  actionnaires?: ActionnaireResponse[];
  comptes?: CompteResponse[];
}

// ============================================================
// Actionnaire (shareholders / associates)
// ============================================================

export interface ActionnaireRequest {
  nom: string;
  nombreActions: number;
  montant?: number;
  pourcentageActions?: number;
  clientId: number;
}

export interface ActionnaireResponse {
  id: number;
  nom: string;
  nombreActions: number;
  montant?: number;
  pourcentageActions?: number;
  clientId: number;
}

// ============================================================
// Compte (bank accounts)
// ============================================================

export interface CompteRequest {
  codeGuichet: string;
  codeProduit: string;
  numCompte: string;
  agence: string;
  clientId: number;
}

export interface CompteResponse {
  codeGuichet: string;
  codeProduit: string;
  numCompte: string;
  numeroCompteComplet: string; // ex: "146-DT13-004344"
  agence: string;
  clientId: number;
  mandataires?: MandataireResponse[];
}

// ============================================================
// Mandataire (authorized signatories)
// ============================================================

export interface MandataireRequest {
  numMandat?: string;
  numDemande?: string;
  typeMandat?: string;
  agence?: string;
  mandant?: string;
  dateCreation?: string;        // ISO date string 'YYYY-MM-DD'
  dateDebut?: string;
  dateFin?: string;
  compteCodeGuichet: string;
  compteCodeProduit: string;
  compteNumCompte: string;
}

export interface MandataireResponse {
  id: number;
  numMandat?: string;
  numDemande?: string;
  typeMandat?: string;
  agence?: string;
  mandant?: string;
  dateCreation?: string;
  dateDebut?: string;
  dateFin?: string;
  compteCodeGuichet: string;
  compteCodeProduit: string;
  compteNumCompte: string;
}
