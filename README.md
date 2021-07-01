# Batalha de Drones

Trabalho 4 para a disciplina INF1771 - Inteligência Artificial, semestre 21.1

Desenvolvido por Daniel Guimarães

## Descrição

A descrição da competição pode ser encontrada [aqui](https://augustobaffa.pro.br/site/desafios-online/inf1771-inteligencia-artificial-desafio-dos-drones/)

Este projeto envolve a construção de uma inteligência artificial para competir numa batalha de “drones”.

## Funcionamento

A inteligência artificial utilizada foi uma *máquina de estados ponderada* , com usos de algoritmos de *pathfinding* 

A cada tique (100ms), é feita uma observação em volta do drone. A próxima decisão deve ser tomada em base dessas observações,
assim como outros detalhes, como o mapa em volta.

As decisões (estados) e os seus pesos foram programados da seguinte maneira:

### Explorar

O objetivo nesse estado é caminhar pelo mapa em lugares ainda não explorados, para conseguir obter mais informações.

**Cálculo do peso**:
```text
1 - peso(Coletar)
```

### Atacar

O objetivo nesse estado é atacar (e se possível, perseguir) um inimigo.

**Cálculo do peso**:
```text
1 - peso(fugir)
```

### Fugir

O objetivo nesse estado é andar em direção ao *powerup* mais próximo de uma maneira um pouco aleatória.
Ele fica nesse estado por 10 ticks (1 tick = 1 ação tomada)

**Cálculo do peso**:
```text
(0.5 + 0.5 * (energiaMinha - energiaInimigo) / 100 ) * energiaMinha / 100
```

### Recarregar

O objetivo nesse estado é chegar em um *powerup* da maneira mais rápida possível, para recarregar as energias do drone.

Nesse caso, será utilizado o *A\** , que fornece o menor caminho até o *powerup* mais próximo que estiver disponível.

**Cálculo do peso**: 
```text
1                           quando Energia < 30
1 - (Energia - 30) / 40     quando 30 < Energia < 70 
0                           quando Energia > 70
```

### Coletar

O objetivo nesse estado é coletar algum ouro, seja na própria posição do drone, ou em alguma posição distante.

**Cálculo do peso**:
```text
quando está na posição do ouro: 1                           
em outros casos:

media de (2/pi * 0.5 * arctan(distancia - tempo) + 0.5)
para cada posição de ouro conhecida, onde
    distancia = distancia do drone até o ouro
    tempo = em quanto tempo o ouro renasce

```

Cada peso varia de 0 até 1. Dentre todos os estados, é escolhido executar as ações do estado com melhor peso.

Cada estado, ao ser escolhido, cria uma lista de ações a serem executadas. Caso ele seja escolhido novamente para decidir
a próxima ação, a lista de ações permanecerá a mesma. Caso seja escolhida um novo estado, a lista de ações será recalculada.

