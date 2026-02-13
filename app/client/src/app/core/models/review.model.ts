export interface Review {
  id: number;
  score: number;
  comment: string;
  createdAt: string;
  userFullName?: string;
}

export interface ReviewCreateRequest {
  score: number;
  comment: string;
  agentId: number;
}
