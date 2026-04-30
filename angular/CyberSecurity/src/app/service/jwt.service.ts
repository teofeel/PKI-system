import { Injectable } from "@angular/core";
import { jwtDecode } from "jwt-decode";
import { JwtTokenPayload } from "../model/JwtTokenPayload";

@Injectable({
  providedIn: 'root',
})
export class JwtService {
    getJwtToken() : JwtTokenPayload | null {
        const jwt: string | null = localStorage.getItem('jwt');

        if (!jwt) return null;

        try{
            return jwtDecode<JwtTokenPayload>(jwt);
        }catch{
            return null;
        }
    }
}

