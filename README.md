# WorldRankup

## Comandos
| Comando                                                   | Descrição                                  |
|-----------------------------------------------------------|--------------------------------------------|
| /rankup                                                   | Abrir GUI de evolução de rank              |
| /ranks                                                    | Mostrar a lista de ranks                   |
| /mochila                                                  | Abrir GUI de gerenciamentos de fragmentos  |
| /rankup setrank <jogador> <rank>                          | Setar o rank de um jogador                 |
| /rankup evoluir <jogador>                                 | Evoluir o rank de um jogador               |
| /rankup regredir <jogador>                                | Regredir o rank de um jogador              |
| /rankup darfragmentos <jogador> <fragmento> <quantia>     | Givar fragmentos para um jogador           |
| /rankup removerfragmentos <jogador> <fragmento> <quantia> | Remover fragmentos de um jogador           |
| /rankup setarfragmentos <jogador> <fragmento> <quantia>   | Setar fragmentos de um jogador             |
| /rankup darlimite <jogador> <fragmento> <quantia>         | Givar limite de fragmentos para um jogador |
| /rankup removerlimite <jogador> <fragmento> <quantia>     | Remover limite fragmentos de um jogador    |
| /rankup setarlimite <jogador> <fragmento> <quantia>       | Setar limite fragmentos de um jogador      |
| /rankup ajuda                                             | Mostrar a lista de comandos                |
| /rankup reload                                            | Recarregar configurações                   |

## Permissões
| Permissão                     | Descrição                                          |
|-------------------------------|----------------------------------------------------|
| worldrankup.setrank           | Executar /rankup setrank                           |
| worldrankup.rankevoluir       | Executar /rankup evoluir                           |
| worldrankup.rankregredir      | Executar /rankup regredir                          |
| worldrankup.darfragmentos     | Executar /rankup darfragmentos                     |
| worldrankup.removerfragmentos | Executar /rankup removerfragmentos                 |
| worldrankup.setarfragmentos   | Executar /rankup setarfragmentos                   |
| worldrankup.darlimite         | Executar /rankup darlimite                         |
| worldrankup.removerlimite     | Executar /rankup removerlimite                     |
| worldrankup.setarlimite       | Executar /rankup setarlimite                       |
| worldrankup.ajudastaff        | Mostrar ajuda para staff ao executar /rankup ajuda |
| worldrankup.reload            | Executar /rankup reload                            |

## Configuração
| Arquivo/pasta   | Função                                                            |
|-----------------|-------------------------------------------------------------------|
| config.yml      | Configurar rank inicial, habilitar venda de fragmentos            |
| ranks.yml       | Configurar ranks (tags, permissões, requisitos, etc)              |
| fragmentos.yml  | Configurar fragmentos (limite, itens, preço, nomes, etc)          |
| recompensas.yml | Configurar ganho de fragmentos (mineração, matar mobs, pesca)     |
| resposta/       | Pasta de configuração de respostas como mensagens, sons e efeitos |
| menu/           | Pasta de configuração de menus                                    |