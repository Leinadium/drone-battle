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

### Atacar

*Condição:* Quando aparece um inimigo na frente do drone, e a energia do drone está acima de 30.

*Ação:* Atirar uma vez

### Fugir

*Condição:* Quando tomar algum dano inimigo, ou quando houver um inimigo (seja em volta ou na frente) e a energia do 
drone está menor ou igual a 30.

Esse estado permanecerá ativo por no mínimo 5 *ticks* .

*Ação:*

* Se o drone tomou algum dano, tenta fugir para um quadrado mais próximo na área 3x3 na frente dele;
* Se houver um inimigo em volta, tenta fugir para um quadrado mais próximo;
* Se houver um inimigo na frente, tenta fugir para o quadrado mais próximo numa área 5x2 dos lados dele.

Uma visualização das áreas abaixo:

```text

-AAA-  AAAAA  AA-AA
-AAA-  A---A  AA-AA
-AAA-  A-X-A  AAXAA
--X--  A---A  AA-AA
-----  AAAAA  AA-AA

```

Se não houver nenhum quadrado disponível, ele faz a ação recomendada para *Recarregar*

Esse estado implementa uma fila de ações, que será explicado mais abaixo.

### Recarregar

*Condição:* Quando a energia do drone estiver abaixo de 30.

*Ação:* Calcula o *powerup* mais próximo (em que os ticks até o powerup nascer menos os ticks até chegar nele sejam o menor possível).

* Se este *powerup* é possível coletar imediatamente, seguir o menor caminho seguro até ele;
* Se este *powerup* não seja possível coletar imediatamente, iniciar uma *exploração com ponto focal nele* (mais explicado abaixo);
* Se não houver *powerup* conhecido, faz o recomendado para *Exploração* .

### Coletar

*Condição:* Quando há algum ouro que seja possível coletar imediatamente (ou seja, os ticks até este renascer menos os ticks
 para chegar neste é igual ou menor que zero).

*Ação:* Seguir o menor caminho conhecido até ele.

Esse estado implementa uma fila de ações, que será explicado mais abaixo.

### Exploração

*Condição:* Quando nenhum dos outros estados estiver ativo

*Ação:* Calcula o **ponto focal** da exploração. O ponto focal é o quadrado inicial do drone quando não se sabe a
localização de nenhum ouro, e é o quadrado médio de todos os ouros conhecidos quando se sabe a localização de algum ouro.
Usando esse ponto focal, é calculado o bloco cuja distância euclidiana ao quadrado do ponto focal somado com a distância mínima
do drone até o bloco é a menor possível. Os blocos verificados são os quatro blocos em volta do drone, e os blocos distantes
da menor distância manhattan do ponto focal que possui algum bloco seguro. 

Se não houver nenhum bloco seguro para explorar, é procurado um bloco que contenha um teleporte. 
Se também não houver, algum bloco que possa ter um buraco (mais arriscado).
Se ainda não houver, então o drone está preso, e não há nada o que fazer.


A fila de ações utilizada em alguns estados é uma fila contendo as próximas ações a serem feitas. Caso na próxima observação
o estado seja mantido, não será recalculada as ações, mas serão utilizadas as ações dessa fila, até que ela esteja vazia.