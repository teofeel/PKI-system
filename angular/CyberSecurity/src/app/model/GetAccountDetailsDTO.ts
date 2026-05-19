import { UserDTO } from './UserDTO'; 

export interface GetAccountDetailsDTO {
  email: string;
  using2FA: boolean;
  user: UserDTO;
}