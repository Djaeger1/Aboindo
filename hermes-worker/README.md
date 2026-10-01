# hermes-worker

Source worker `hermes-v2` (Cloudflare Workers).

- `_worker.js` disimpan terpotong menjadi `_worker.js.part1` .. `_worker.js.part5`
  karena batas ukuran per-request GitHub API. Isi 100% utuh, tinggal digabung:
  ```
  cat _worker.js.part* > _worker.js
  ```
- `meta.json` = metadata deploy (bindings). Nilai secret disamarkan
  (`GANTI_DENGAN_NILAI_ASLI`) karena repo ini publik — isi nilai asli dari
  Cloudflare dashboard saat deploy.
