#!/usr/bin/env bash
#  Baja los últimos cambios de una rama y actualiza el sistema desplegado.
#
#  Uso (en la VM):
#     ~/DonaTrack/deploy/actualizar.sh            -> rama main
#     ~/DonaTrack/deploy/actualizar.sh dev        -> rama dev

set -euo pipefail

RAMA="${1:-main}" #Toma el argumento ($1), si no: main
RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)" #Si la saco del repo y la dejo solo en la VM: RAIZ="$HOME/DonaTrack"
cd "$RAIZ"

echo "==> [1/4] Bajando cambios de la rama '$RAMA'..."
git fetch origin "$RAMA"
git checkout "$RAMA"
git pull --ff-only origin "$RAMA"
echo "    Commit desplegado: $(git log -1 --format='%h %s (%an, %ar)')"

echo "==> [2/4] Reconstruyendo imágenes y recreando los contenedores que cambiaron..."
docker compose up -d --build

echo "==> [3/4] Borrando imágenes viejas..."
docker image prune -f > /dev/null

echo "==> [4/4] Estado actual:"
docker compose ps

echo
echo "Listo. Logs en vivo:  docker compose logs -f bff   (o donaciones / logistica / mysql)"
