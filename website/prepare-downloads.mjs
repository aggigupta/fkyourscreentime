import {readFile,writeFile,mkdir} from 'node:fs/promises';
import {createHash} from 'node:crypto';
const file=new URL('./downloads/enough-0.2.0-alpha.apk',import.meta.url);
const expected=(await readFile(new URL('./downloads/enough-0.2.0-alpha.sha256',import.meta.url),'utf8')).trim().split(/\s+/)[0];
if(!/^[a-f0-9]{64}$/.test(expected))throw Error('A valid release checksum is required.');
let bytes;
try{bytes=await readFile(file);}catch(error){
 if(error.code!=='ENOENT')throw error;
 const response=await fetch('https://github.com/aggigupta/fkyourscreentime/releases/download/v0.2.0-alpha/Enough-0.2.0-alpha.apk');
 if(!response.ok)throw Error('Release APK is unavailable: '+response.status);
 bytes=Buffer.from(await response.arrayBuffer());
}
if(createHash('sha256').update(bytes).digest('hex')!==expected)throw Error('Release APK checksum mismatch; refusing to deploy it.');
await mkdir(new URL('./downloads/',import.meta.url),{recursive:true});
await writeFile(file,bytes);
console.log('Verified the Android release APK ('+bytes.length+' bytes).');
