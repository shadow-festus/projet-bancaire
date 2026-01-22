/**
 * Interfaces pour l'authentification
 */

/** Payload pour la connexion */
export interface LoginPayload {
  username: string;
  password: string;
}

/** Payload pour l'inscription */
export interface RegisterPayload {
  username: string;
  email: string;
  password: string;
}

/** Réponse d'authentification du serveur */
export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  username: string;
  email: string;
  role: string;
  clientId?: number;
}

/** Informations utilisateur extraites du token */
export interface UserInfo {
  username: string;
  exp: number;
}
