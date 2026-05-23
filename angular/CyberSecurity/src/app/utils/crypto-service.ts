import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class CryptoService {
  private readonly algorithm = {
    name: 'RSA-OAEP',
    modulusLength: 2048,
    publicExponent: new Uint8Array([1, 0, 1]),
    hash: 'SHA-256',
  };

  async generateKeys(): Promise<CryptoKeyPair> {
    return await window.crypto.subtle.generateKey(this.algorithm, true, ['encrypt', 'decrypt']);
  }

  async exportPublicKey(key: CryptoKey): Promise<string> {
    const exported = await window.crypto.subtle.exportKey('spki', key);
    return btoa(String.fromCharCode(...new Uint8Array(exported)));
  }

  async exportPrivateKey(key: CryptoKey): Promise<string> {
    const exported = await window.crypto.subtle.exportKey('pkcs8', key);
    const b64 = btoa(String.fromCharCode(...new Uint8Array(exported)));
    return `-----BEGIN PRIVATE KEY-----\n${b64.match(/.{1,64}/g)?.join('\n')}\n-----END PRIVATE KEY-----`;
  }

  async importPrivateKey(pem: string): Promise<CryptoKey> {
    const b64 = pem
      .replace(/-----BEGIN PRIVATE KEY-----/, '')
      .replace(/-----END PRIVATE KEY-----/, '')
      .replace(/\s/g, '');

    const binaryDer = Uint8Array.from(atob(b64), (c) => c.charCodeAt(0));

    return await window.crypto.subtle.importKey('pkcs8', binaryDer.buffer, this.algorithm, true, [
      'decrypt',
    ]);
  }

  async decryptMessage(encryptedString: string, privateKey: CryptoKey): Promise<string> {
    const cleanBase64 = encryptedString.replace(/\s+/g, '');

    const encryptedBuffer = Uint8Array.from(window.atob(cleanBase64), (c) => c.charCodeAt(0));
    const decryptedBuffer = await window.crypto.subtle.decrypt(
      { name: 'RSA-OAEP' },
      privateKey,
      encryptedBuffer,
    );

    return new TextDecoder().decode(decryptedBuffer);
  }
}
