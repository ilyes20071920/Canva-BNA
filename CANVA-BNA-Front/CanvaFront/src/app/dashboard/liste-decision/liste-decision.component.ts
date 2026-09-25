import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';

export interface SollicitationListItem {
  id: number;
  clientId: number;
  clientIdentifiant?: string;
  clientRelation?: string;
  clientGroupe?: string;
  dateDemande: string;
  produit?: { id: number; code: string; libelle: string };
  objetCredit?: { id: number; code: string; libelle: string };
  montantSollicite: number;
  dureeSolliciteeMois: number;
  statut: string;
  decision?: string;
  commentaireDecision?: string;
  dateDecision?: string;
}

@Component({
  selector: 'app-liste-decision',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './liste-decision.component.html',
  styleUrl: './liste-decision.component.scss'
})
export class ListeDecisionComponent implements OnInit {
  private http = inject(HttpClient);
  private router = inject(Router);
  private baseUrl = 'http://localhost:8089/api';

  sollicitations: SollicitationListItem[] = [];
  filteredSollicitations: SollicitationListItem[] = [];
  isLoading: boolean = false;
  errorMessage: string = '';
  successMessage: string = '';

  // Search and Filter State
  searchTerm: string = '';
  selectedStatutFilter: string = 'TOUS';

  // Status Change Modal State
  showStatusModal: boolean = false;
  selectedItemToUpdate: SollicitationListItem | null = null;
  newStatusValue: string = 'EN_ATTENTE_DECISION';
  statusCommentaire: string = '';
  isUpdatingStatus: boolean = false;

  ngOnInit(): void {
    this.loadDecisions();
  }

  getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  loadDecisions(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.http.get<SollicitationListItem[]>(`${this.baseUrl}/clients/sollicitations/all`, { headers: this.getHeaders() })
      .subscribe({
        next: (data) => {
          this.sollicitations = data || [];
          this.applyFilter();
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Error loading decisions', err);
          this.errorMessage = 'Erreur lors du chargement des décisions.';
          this.isLoading = false;
        }
      });
  }

  applyFilter(): void {
    const term = this.searchTerm.trim().toLowerCase();
    
    this.filteredSollicitations = this.sollicitations.filter(item => {
      // Filter by status
      const matchesStatus = this.selectedStatutFilter === 'TOUS' || item.statut === this.selectedStatutFilter;
      
      // Filter by search text
      const matchesSearch = !term ||
        (item.clientIdentifiant && item.clientIdentifiant.toLowerCase().includes(term)) ||
        (item.clientRelation && item.clientRelation.toLowerCase().includes(term)) ||
        (item.clientGroupe && item.clientGroupe.toLowerCase().includes(term)) ||
        (item.produit?.libelle && item.produit.libelle.toLowerCase().includes(term)) ||
        (item.commentaireDecision && item.commentaireDecision.toLowerCase().includes(term)) ||
        (item.montantSollicite && item.montantSollicite.toString().includes(term));

      return matchesStatus && matchesSearch;
    });
  }

  onSearchChange(): void {
    this.applyFilter();
  }

  onStatutFilterChange(statut: string): void {
    this.selectedStatutFilter = statut;
    this.applyFilter();
  }

  // Count Getters for Stats
  get countTotal(): number {
    return this.sollicitations.length;
  }

  get countPending(): number {
    return this.sollicitations.filter(s => s.statut === 'EN_ATTENTE_DECISION').length;
  }

  get countAccepted(): number {
    return this.sollicitations.filter(s => s.statut === 'ACCEPTE').length;
  }

  get countRefused(): number {
    return this.sollicitations.filter(s => s.statut === 'REFUSE').length;
  }

  // Modal Actions
  openUpdateStatusModal(item: SollicitationListItem): void {
    this.selectedItemToUpdate = item;
    this.newStatusValue = item.statut || 'EN_ATTENTE_DECISION';
    this.statusCommentaire = item.commentaireDecision || '';
    this.showStatusModal = true;
  }

  closeUpdateStatusModal(): void {
    this.showStatusModal = false;
    this.selectedItemToUpdate = null;
    this.statusCommentaire = '';
  }

  saveStatusUpdate(): void {
    if (!this.selectedItemToUpdate) return;

    this.isUpdatingStatus = true;
    const clientId = this.selectedItemToUpdate.clientId;

    const url = `${this.baseUrl}/clients/${clientId}/sollicitation/statut?statut=${this.newStatusValue}&commentaire=${encodeURIComponent(this.statusCommentaire || '')}`;

    this.http.put<SollicitationListItem>(url, {}, { headers: this.getHeaders() })
      .subscribe({
        next: (updated) => {
          this.successMessage = `Statut mis à jour avec succès pour le client ${this.selectedItemToUpdate?.clientIdentifiant || clientId}.`;
          this.closeUpdateStatusModal();
          this.isUpdatingStatus = false;
          this.loadDecisions();
        },
        error: (err) => {
          console.error('Error updating status', err);
          this.errorMessage = 'Erreur lors de la mise à jour du statut.';
          this.isUpdatingStatus = false;
        }
      });
  }

  clearMessages(): void {
    this.successMessage = '';
    this.errorMessage = '';
  }
}
