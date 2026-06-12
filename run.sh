#!/bin/bash

# Diretório do projeto
PROJECT_DIR="/media/astral/7DFD-F7FB/Folia/SolanaDevMinecraft/Solana-Forge-1.20.1"
# Diretório de destino
DEST_DIR="/home/astral/Documents/curseforge/minecraft/Instances/Solana-Sonic/mods"

echo "======================================"
echo "Iniciando compilação do Mod SolanaForge..."
echo "======================================"

cd "$PROJECT_DIR" || exit 1

# Garante que o gradlew é executável
chmod +x gradlew

# Executa o build (limpa e constrói)
./gradlew clean build

# Verifica se a compilação foi bem-sucedida
if [ $? -eq 0 ]; then
    echo ""
    echo "======================================"
    echo "Compilação concluída com sucesso!"
    echo "Copiando o mod para a pasta de mods do CurseForge..."
    echo "======================================"

    # Cria o diretório de destino se não existir
    mkdir -p "$DEST_DIR"

    # Encontra o arquivo .jar compilado (ignorando o sources/slim jars se houver) e copia
    # O arquivo principal geralmente não tem sufixo extra ou termina em -1.20.1.jar, mas pegaremos o mod
    find build/libs -name "*.jar" ! -name "*-sources.jar" ! -name "*-slim.jar" -exec cp -v {} "$DEST_DIR/" \;
    
    echo "======================================"
    echo "Mod copiado para: $DEST_DIR"
    echo "======================================"
else
    echo "======================================"
    echo "Erro durante a compilação do mod! Verifique os logs acima."
    echo "======================================"
    exit 1
fi
