package br.com.fiap.satelite.config.vector;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configura o VectorStore em memória (SimpleVectorStore) com documentos
 * dos protocolos de contingência da Defesa Civil — base de conhecimento para o RAG.
 */
@Configuration
public class VectorStoreConfig {

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore store = SimpleVectorStore.builder(embeddingModel).build();

        // -------------------------------------------------------
        // Base de conhecimento: protocolos da Defesa Civil (RAG)
        // -------------------------------------------------------
        List<Document> protocolos = List.of(
                new Document("""
                        PROTOCOLO DEFESA CIVIL — RISCO DE INCÊNDIO FLORESTAL (Nível CRÍTICO)
                        Gatilho: Umidade relativa < 15% combinada com temperatura > 35°C.
                        Ações imediatas:
                        1. Emitir alerta vermelho para municípios no raio de 50km.
                        2. Acionar brigadistas e helicópteros de combate.
                        3. Interditar estradas de acesso à área de risco.
                        4. Iniciar evacuação preventiva de comunidades rurais isoladas.
                        Responsável: Coordenadoria Estadual de Proteção e Defesa Civil (CEPDEC).
                        """),

                new Document("""
                        PROTOCOLO DEFESA CIVIL — ENCHENTE E ALAGAMENTO (Nível CRÍTICO)
                        Gatilho: Precipitação acumulada > 150mm em 24h ou > 250mm em 72h.
                        Ações imediatas:
                        1. Ativar Centro de Operações de Emergência (COE).
                        2. Evacuar áreas de várzea e margens de rios em até 2 horas.
                        3. Abrir abrigos em escolas e ginásios municipais.
                        4. Interromper tráfego em vias alagadas.
                        5. Acionar Corpo de Bombeiros e equipes de resgate aquático.
                        Indicador de alerta: Subida do nível do rio acima da cota de inundação.
                        """),

                new Document("""
                        PROTOCOLO DEFESA CIVIL — SECA EXTREMA E DESERTIFICAÇÃO
                        Gatilho: Precipitação < 5mm por 30 dias consecutivos e umidade < 20%.
                        Ações:
                        1. Decretar estado de emergência hídrica no município.
                        2. Acionar operação carro-pipa para abastecimento.
                        3. Restringir uso de água para atividades não essenciais.
                        4. Monitorar saúde de animais e lavouras.
                        Regiões prioritárias: Semiárido nordestino, norte de Minas Gerais.
                        """),

                new Document("""
                        PROTOCOLO DEFESA CIVIL — TEMPESTADE SEVERA E VENDAVAL
                        Gatilho: Velocidade do vento > 90km/h com precipitação intensa.
                        Ações imediatas:
                        1. Emitir alerta de ventos fortes via Sistema de Alertas da Defesa Civil (SIADC).
                        2. Suspender eventos ao ar livre e obras em altura.
                        3. Reforçar fiscalização de estruturas precárias e moradores de encostas.
                        4. Acionar equipes de motoserras para remoção de árvores caídas.
                        Observação: Em caso de granizo, acionar seguro agrícola emergencial.
                        """),

                new Document("""
                        PROTOCOLO DEFESA CIVIL — CALOR EXTREMO
                        Gatilho: Temperatura > 40°C por 3 dias consecutivos ou > 44°C pontual.
                        Ações:
                        1. Ativar salas de resfriamento em pontos públicos (shopping, igrejas, postos de saúde).
                        2. Distribuir água potável em locais de alta circulação.
                        3. Emitir orientações de saúde: evitar exposição entre 10h e 16h.
                        4. Monitorar hospitais e UPAs para aumento de casos de insolação.
                        Grupos vulneráveis: idosos, crianças, trabalhadores externos.
                        """)
        );

        store.add(protocolos);
        return store;
    }
}
