// Enums

export type NotificationType =
  // Student-facing
  | 'JOB_APPLICATION_ACCEPTED'
  | 'JOB_APPLICATION_REJECTED'
  | 'CERTIFICATE_APPROVED'
  | 'CERTIFICATE_REJECTED'
  | 'UNIVERSITY_LINKED'
  // Company-facing
  | 'JOB_APPLICATION_RECEIVED'
  | 'PARTNERSHIP_ACCEPTED'
  | 'PARTNERSHIP_REJECTED'
  // University-facing
  | 'CERTIFICATE_SUBMITTED'
  | 'PARTNERSHIP_REQUESTED'
  // Cross-cutting
  | 'CHAT_MESSAGE_RECEIVED'
  // System — sent once after first-time email verification
  | 'WELCOME';

// Responses
export interface NotificationItem {
  id: string;
  type: NotificationType;
  title: string;
  body: string;
  referenceId: string | null;
  referenceType: string | null;
  read: boolean;
  createdAt: string;   // ISO-8601 LocalDateTime from backend
  readAt: string | null;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPage: number;   // backend uses "totalPage" (singular), not "totalPages"
  last: boolean;
}

export interface NotificationPreference {
  userId: string;
  notificationType: NotificationType;
  inAppEnabled: boolean;
}

// Requests
export interface PreferenceEntry {
  notificationType: NotificationType;
  inAppEnabled: boolean;
}

export interface UpdatePreferencesRequest {
  preferences: PreferenceEntry[];
}
