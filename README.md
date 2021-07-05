# Batalha de Drones

Trabalho 4 para a disciplina INF1771 - Inteligência Artificial, semestre 21.1

Desenvolvido por Daniel Guimarães

## Descrição

A descrição da competição pode ser encontrada [aqui](https://augustobaffa.pro.br/site/desafios-online/inf1771-inteligencia-artificial-desafio-dos-drones/)

Este projeto envolve a construção de uma inteligência artificial para competir numa batalha de “drones”.

## Funcionamento

A inteligência artificial utilizada foi uma *máquina de estados* , com usos de algoritmos de *pathfinding* 

A cada tick (100ms), é feita uma observação em volta do drone. A próxima decisão deve ser tomada em base dessas observações,
assim como outros detalhes, como o mapa em volta.

Os estados estão abaixo, em ordem decrescente de relevância:

* **Atacar** : Ataca o inimigo que encotrou

* **Fugir** : Tenta fugir dos disparos inimigos

* **Recarregar** : Tenta recarregar a sua energia o mais rápido possível

* **Coletar** : Coleta o ouro

* **Explorar** : Explora o mapa de acordo com os algoritmos criados