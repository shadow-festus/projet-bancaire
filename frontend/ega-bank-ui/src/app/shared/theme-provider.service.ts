import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ThemeProviderService {
  toggleDark(enable?: boolean) {
    const body = document.body;
    const isDark = body.classList.contains('dark');
    if (enable === undefined) {
      body.classList.toggle('dark');
    } else if (enable && !isDark) {
      body.classList.add('dark');
    } else if (!enable && isDark) {
      body.classList.remove('dark');
    }
  }
}
