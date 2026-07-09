import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';

export interface User {
  id?: number;
  matricule: number;
  nom?: string;
  prenom?: string;
  structure?: string;
  role: string;
  enabled?: boolean;
  password?: string;
}

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './user-management.component.html',
  styleUrl: './user-management.component.scss'
})
export class UserManagementComponent implements OnInit {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8089/api/users';

  users: User[] = [];
  showAddModal = false;
  showEditModal = false;

  newUser: User = {
    matricule: 0,
    nom: '',
    prenom: '',
    password: '',
    structure: '',
    role: 'ROLE_PRISE_EN_CHARGE'
  };

  editUser: User = {
    matricule: 0,
    nom: '',
    prenom: '',
    structure: '',
    role: 'ROLE_PRISE_EN_CHARGE'
  };

  private editOriginalMatricule: number = 0;

  // Sort state
  sortColumn: string = '';
  sortDirection: 'asc' | 'desc' | '' = '';

  // Filter state
  filterNom: string = '';
  filterPrenom: string = '';
  filterMatricule: string = '';
  filterStructure: string = '';
  filterRole: string = 'Choisir ...';
  filterStatut: string = 'Choisir ...';

  // Notifications
  successMessage: string = '';
  errorMessage: string = '';

  ngOnInit(): void {
    this.loadUsers();
  }

  getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  loadUsers(): void {
    this.http.get<User[]>(this.apiUrl, { headers: this.getHeaders() })
      .subscribe({
        next: (data) => {
          this.users = data;
        },
        error: (err) => console.error('Error loading users', err)
      });
  }

  get filteredAndSortedUsers(): User[] {
    let result = [...this.users];

    if (this.filterNom) {
      result = result.filter(u => u.nom?.toLowerCase().includes(this.filterNom.toLowerCase()));
    }
    if (this.filterPrenom) {
      result = result.filter(u => u.prenom?.toLowerCase().includes(this.filterPrenom.toLowerCase()));
    }
    if (this.filterMatricule) {
      result = result.filter(u => u.matricule.toString().includes(this.filterMatricule));
    }
    if (this.filterStructure && this.filterStructure !== 'Choisir ...') {
      result = result.filter(u => u.structure?.toLowerCase().includes(this.filterStructure.toLowerCase()));
    }
    if (this.filterRole && this.filterRole !== 'Choisir ...') {
      result = result.filter(u => u.role === this.filterRole);
    }
    if (this.filterStatut && this.filterStatut !== 'Choisir ...') {
      const isEnabled = this.filterStatut === 'true';
      result = result.filter(u => !!u.enabled === isEnabled);
    }

    if (this.sortColumn && this.sortDirection) {
      result.sort((a, b) => {
        let valA = (a as any)[this.sortColumn];
        let valB = (b as any)[this.sortColumn];

        if (typeof valA === 'string') valA = valA.toLowerCase();
        if (typeof valB === 'string') valB = valB.toLowerCase();

        if (valA === undefined || valA === null) valA = '';
        if (valB === undefined || valB === null) valB = '';

        if (valA < valB) return this.sortDirection === 'asc' ? -1 : 1;
        if (valA > valB) return this.sortDirection === 'asc' ? 1 : -1;
        return 0;
      });
    }

    return result;
  }

  sortBy(column: string): void {
    if (this.sortColumn === column) {
      if (this.sortDirection === 'asc') this.sortDirection = 'desc';
      else if (this.sortDirection === 'desc') this.sortDirection = '';
      else this.sortDirection = 'asc';
    } else {
      this.sortColumn = column;
      this.sortDirection = 'asc';
    }
    if (this.sortDirection === '') {
      this.sortColumn = '';
    }
  }

  /* ---- CREATE ---- */
  openAddModal(): void {
    this.newUser = { matricule: 0, nom: '', prenom: '', password: '', structure: '', role: 'ROLE_PRISE_EN_CHARGE' };
    this.showAddModal = true;
  }

  closeAddModal(): void {
    this.showAddModal = false;
  }

  saveUser(): void {
    this.clearMessages();
    this.http.post<User>(this.apiUrl, this.newUser, { headers: this.getHeaders() })
      .subscribe({
        next: () => {
          this.successMessage = 'Utilisateur créé avec succès.';
          this.loadUsers();
          this.closeAddModal();
        },
        error: (err) => {
          this.errorMessage = 'Erreur lors de la création de l\'utilisateur.';
          console.error('Error saving user', err);
        }
      });
  }

  /* ---- EDIT ---- */
  openEditModal(user: User): void {
    this.editOriginalMatricule = user.matricule;
    this.editUser = { ...user, password: '' };
    this.showEditModal = true;
  }

  closeEditModal(): void {
    this.showEditModal = false;
  }

  updateUser(): void {
    this.clearMessages();
    const payload = {
      nom: this.editUser.nom,
      prenom: this.editUser.prenom,
      structure: this.editUser.structure,
      role: this.editUser.role,
      password: this.editUser.password
    };
    this.http.put<User>(`${this.apiUrl}/${this.editOriginalMatricule}`, payload, { headers: this.getHeaders() })
      .subscribe({
        next: () => {
          this.successMessage = 'Utilisateur modifié avec succès.';
          this.loadUsers();
          this.closeEditModal();
        },
        error: (err) => {
          this.errorMessage = 'Erreur lors de la modification de l\'utilisateur.';
          console.error('Error updating user', err);
        }
      });
  }

  /* ---- DELETE ---- */
  deleteUser(user: User): void {
    this.clearMessages();
    if (!confirm(`Supprimer l'utilisateur ${user.nom} ${user.prenom} (${user.matricule}) ?`)) {
      return;
    }
    this.http.delete(`${this.apiUrl}/${user.matricule}`, { headers: this.getHeaders() })
      .subscribe({
        next: () => {
          this.successMessage = 'Utilisateur supprimé avec succès.';
          this.loadUsers();
        },
        error: (err) => {
          this.errorMessage = 'Erreur lors de la suppression de l\'utilisateur.';
          console.error('Error deleting user', err);
        }
      });
  }

  /* ---- ACTIVATE / DEACTIVATE ---- */
  toggleEnabled(user: User): void {
    this.clearMessages();
    const action = user.enabled ? 'disable' : 'enable';
    this.http.patch<User>(`${this.apiUrl}/${user.matricule}/${action}`, {}, { headers: this.getHeaders() })
      .subscribe({
        next: () => {
          this.successMessage = user.enabled ? 'Utilisateur désactivé.' : 'Utilisateur activé.';
          this.loadUsers();
        },
        error: (err) => {
          this.errorMessage = 'Erreur lors du changement de statut de l\'utilisateur.';
          console.error('Error toggling user', err);
        }
      });
  }

  private clearMessages(): void {
    this.successMessage = '';
    this.errorMessage = '';
  }
}
