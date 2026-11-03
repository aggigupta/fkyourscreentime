const CACHE = 'enough-web-v2';
const SHELL = ['/', '/index.html', '/styles.css', '/app.js', '/focus-engine.js', '/favicon.svg', '/manifest.webmanifest'];
self.addEventListener('install', event => event.waitUntil(caches.open(CACHE).then(cache => cache.addAll(SHELL))));
self.addEventListener('activate', event => event.waitUntil(caches.keys().then(keys => Promise.all(keys.filter(key => key.startsWith('enough-web-') && key !== CACHE).map(key => caches.delete(key))))));
self.addEventListener('fetch', event => {
 if(event.request.method !== 'GET' || new URL(event.request.url).origin !== self.location.origin || new URL(event.request.url).pathname.startsWith('/downloads/'))return;
 event.respondWith(fetch(event.request).catch(() => caches.match(event.request).then(cached => cached || (event.request.mode==='navigate' ? caches.match('/index.html') : Response.error()))));
});
