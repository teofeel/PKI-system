import { JwtPayload } from "jwt-decode";

export interface JwtTokenPayload extends JwtPayload {
    id: number;
    type: string;
}