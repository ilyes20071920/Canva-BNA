// client.service.ts
// Angular service for the business module — manages Client, Actionnaire, Compte, Mandataire, Structure CRUD

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  ClientRequest, ClientResponse,
  ActionnaireRequest, ActionnaireResponse,
  CompteRequest, CompteResponse,
  MandataireRequest, MandataireResponse,
  StructureRequest, StructureResponse,
} from '../models/client.model';

const API_BASE = 'http://localhost:8089/api';

@Injectable({ providedIn: 'root' })
export class ClientService {

  constructor(private readonly http: HttpClient) {}

  // ================================================================ Structure
  // ================================================================

  getAllStructures(): Observable<StructureResponse[]> {
    return this.http.get<StructureResponse[]>(`${API_BASE}/structures`);
  }

  getStructureById(id: number): Observable<StructureResponse> {
    return this.http.get<StructureResponse>(`${API_BASE}/structures/${id}`);
  }

  createStructure(request: StructureRequest): Observable<StructureResponse> {
    return this.http.post<StructureResponse>(`${API_BASE}/structures`, request);
  }

  updateStructure(id: number, request: StructureRequest): Observable<StructureResponse> {
    return this.http.put<StructureResponse>(`${API_BASE}/structures/${id}`, request);
  }

  deleteStructure(id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE}/structures/${id}`);
  }

  // ================================================================ Client
  // ================================================================

  getAllClients(): Observable<ClientResponse[]> {
    return this.http.get<ClientResponse[]>(`${API_BASE}/clients`);
  }

  getClientById(id: number): Observable<ClientResponse> {
    return this.http.get<ClientResponse>(`${API_BASE}/clients/${id}`);
  }

  getClientByIdentifiant(identifiant: string): Observable<ClientResponse> {
    return this.http.get<ClientResponse>(`${API_BASE}/clients/identifiant/${identifiant}`);
  }

  createClient(request: ClientRequest): Observable<ClientResponse> {
    return this.http.post<ClientResponse>(`${API_BASE}/clients`, request);
  }

  updateClient(id: number, request: ClientRequest): Observable<ClientResponse> {
    return this.http.put<ClientResponse>(`${API_BASE}/clients/${id}`, request);
  }

  deleteClient(id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE}/clients/${id}`);
  }

  // ================================================================ Actionnaire
  // ================================================================

  getActionnairesByClientId(clientId: number): Observable<ActionnaireResponse[]> {
    return this.http.get<ActionnaireResponse[]>(`${API_BASE}/actionnaires/client/${clientId}`);
  }

  getActionnaireById(id: number): Observable<ActionnaireResponse> {
    return this.http.get<ActionnaireResponse>(`${API_BASE}/actionnaires/${id}`);
  }

  createActionnaire(request: ActionnaireRequest): Observable<ActionnaireResponse> {
    return this.http.post<ActionnaireResponse>(`${API_BASE}/actionnaires`, request);
  }

  updateActionnaire(id: number, request: ActionnaireRequest): Observable<ActionnaireResponse> {
    return this.http.put<ActionnaireResponse>(`${API_BASE}/actionnaires/${id}`, request);
  }

  deleteActionnaire(id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE}/actionnaires/${id}`);
  }

  // ================================================================ Compte
  // ================================================================

  getComptesByClientId(clientId: number): Observable<CompteResponse[]> {
    return this.http.get<CompteResponse[]>(`${API_BASE}/comptes/client/${clientId}`);
  }

  getCompteById(codeGuichet: string, codeProduit: string, numCompte: string): Observable<CompteResponse> {
    return this.http.get<CompteResponse>(`${API_BASE}/comptes/${codeGuichet}/${codeProduit}/${numCompte}`);
  }

  createCompte(request: CompteRequest): Observable<CompteResponse> {
    return this.http.post<CompteResponse>(`${API_BASE}/comptes`, request);
  }

  updateCompte(codeGuichet: string, codeProduit: string, numCompte: string, request: CompteRequest): Observable<CompteResponse> {
    return this.http.put<CompteResponse>(`${API_BASE}/comptes/${codeGuichet}/${codeProduit}/${numCompte}`, request);
  }

  deleteCompte(codeGuichet: string, codeProduit: string, numCompte: string): Observable<void> {
    return this.http.delete<void>(`${API_BASE}/comptes/${codeGuichet}/${codeProduit}/${numCompte}`);
  }

  // ================================================================ Mandataire
  // ================================================================

  getMandatairesByCompte(codeGuichet: string, codeProduit: string, numCompte: string): Observable<MandataireResponse[]> {
    return this.http.get<MandataireResponse[]>(`${API_BASE}/mandataires/compte/${codeGuichet}/${codeProduit}/${numCompte}`);
  }

  getMandataireById(id: number): Observable<MandataireResponse> {
    return this.http.get<MandataireResponse>(`${API_BASE}/mandataires/${id}`);
  }

  createMandataire(request: MandataireRequest): Observable<MandataireResponse> {
    return this.http.post<MandataireResponse>(`${API_BASE}/mandataires`, request);
  }

  updateMandataire(id: number, request: MandataireRequest): Observable<MandataireResponse> {
    return this.http.put<MandataireResponse>(`${API_BASE}/mandataires/${id}`, request);
  }

  deleteMandataire(id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE}/mandataires/${id}`);
  }
}
