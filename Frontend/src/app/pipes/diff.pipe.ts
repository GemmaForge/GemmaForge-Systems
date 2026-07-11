import { Pipe, PipeTransform } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

@Pipe({
  name: 'diffHighlighter',
  standalone: true
})
export class DiffHighlighterPipe implements PipeTransform {
  constructor(private sanitizer: DomSanitizer) {}

  transform(value: string | undefined): SafeHtml {
    if (!value) return '';
    const highlighted = value
      .replace(/\[MODIFIED\]/g, '<span class="bg-indigo-600/50 border-l-4 border-indigo-400 px-1 rounded-sm text-white">')
      .replace(/\[\/MODIFIED\]/g, '</span>');
    return this.sanitizer.bypassSecurityTrustHtml(highlighted);
  }
}
