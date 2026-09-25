import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AuthService } from '../auth.service';

export interface DecisionNotification {
  id: number;
  clientId: number;
  clientIdentifiant: string;
  clientRelation: string;
  statut: string;
  decision?: string;
  montantSollicite: number;
  dateDemande: string;
  dateDecision?: string;
  isRead?: boolean;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit, OnDestroy {
  private authService = inject(AuthService);
  private router = inject(Router);
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8089/api';

  user: any = null;
  todayDate: string = '';
  pendingCount: number = 0;

  // Notification system
  notifications: DecisionNotification[] = [];
  unreadCount: number = 0;
  showNotificationDropdown: boolean = false;
  latestToast: DecisionNotification | null = null;
  private pollInterval: any = null;
  private lastKnownDecisionIds = new Set<number>();
  private readonly STORAGE_KEY_SEEN = 'bna_seen_decision_ids';

  ngOnInit(): void {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      this.user = JSON.parse(userStr);
    }
    
    const today = new Date();
    this.todayDate = today.toISOString().split('T')[0];

    // If already on /dashboard/liste-decision on initial load, mark as read
    if (this.router.url.includes('/dashboard/liste-decision')) {
      this.unreadCount = 0;
    }

    if (this.isPriseEnCharge) {
      this.loadNotifications(true);
      // Poll every 10 seconds for real-time notifications
      this.pollInterval = setInterval(() => {
        this.loadNotifications(false);
      }, 10000);
    }
  }

  ngOnDestroy(): void {
    if (this.pollInterval) {
      clearInterval(this.pollInterval);
    }
  }

  get isPriseEnCharge(): boolean {
    return this.user?.role === 'ROLE_PRISE_EN_CHARGE';
  }

  private getSeenIds(): Set<number> {
    try {
      const stored = localStorage.getItem(this.STORAGE_KEY_SEEN);
      return stored ? new Set(JSON.parse(stored)) : new Set();
    } catch {
      return new Set();
    }
  }

  private saveSeenIds(ids: Set<number>): void {
    try {
      localStorage.setItem(this.STORAGE_KEY_SEEN, JSON.stringify(Array.from(ids)));
    } catch (e) {
      console.error('Error saving seen IDs', e);
    }
  }

  markAllAsRead(): void {
    this.unreadCount = 0;
    // Mark all current known IDs as seen
    const seenIds = this.getSeenIds();
    this.lastKnownDecisionIds.forEach(id => seenIds.add(id));
    this.saveSeenIds(seenIds);
  }

  toggleNotifications(): void {
    this.showNotificationDropdown = !this.showNotificationDropdown;
    if (this.showNotificationDropdown) {
      this.markAllAsRead();
    }
  }

  closeNotifications(): void {
    this.showNotificationDropdown = false;
  }

  dismissToast(): void {
    this.latestToast = null;
  }

  goToDecisions(): void {
    this.showNotificationDropdown = false;
    this.latestToast = null;
    this.markAllAsRead();
    this.router.navigate(['/dashboard/liste-decision']);
  }

  onSidebarDecisionClick(): void {
    this.markAllAsRead();
  }

  loadNotifications(isInitial: boolean = false): void {
    const token = localStorage.getItem('token');
    if (!token) return;
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);

    this.http.get<any[]>(`${this.baseUrl}/clients/sollicitations/all`, { headers })
      .subscribe({
        next: (items) => {
          const list = items || [];
          this.pendingCount = list.filter(i => i.statut === 'EN_ATTENTE_DECISION').length;

          // Build notification list (sorted by latest date)
          const sorted = [...list].reverse();
          const seenIds = this.getSeenIds();

          // Calculate unseen items based on localStorage
          const unseenItems = sorted.filter(item => !seenIds.has(item.id));

          if (!isInitial) {
            // Check if there are newly arrived decisions
            const brandNewItems = sorted.filter(item => !this.lastKnownDecisionIds.has(item.id));
            if (brandNewItems.length > 0) {
              const newest = brandNewItems[0];
              this.latestToast = newest;
              setTimeout(() => {
                this.latestToast = null;
              }, 6000);
            }
          }

          // If the user is currently looking at the decision page, keep unread at 0
          if (this.router.url.includes('/dashboard/liste-decision')) {
            this.markAllAsRead();
          } else {
            this.unreadCount = unseenItems.length;
          }

          // Update known IDs
          this.lastKnownDecisionIds.clear();
          sorted.forEach(s => this.lastKnownDecisionIds.add(s.id));

          this.notifications = sorted.slice(0, 8); // Top 8 most recent
        },
        error: (err) => console.error('Error fetching notifications', err)
      });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}

