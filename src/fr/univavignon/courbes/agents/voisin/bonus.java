package fr.univavignon.courbes.agents.voisin;

@SuppressWarnings("javadoc")
public class bonus {
//TODO
}

////////////////////////////////////////////////////////////////
////TRAITEMENT DES BONUS
////////////////////////////////////////////////////////////////
/*
/**
* Choisi un bonus.
* @param var contient un int qui influ sur la note suivant la situation du snake.
* @return un tableau d'int qui va contenir les coordonées des bonus x en [0][] r=et y en [1][]; si le nombre de bonus est de zero, resutl[0][0]==-1
* sinon resutl[0][0] contien la taille;
*/
/*
/**
* @return un tableau d'int qui va contenir les coordonées des bonus x en [0][] r=et y en [1][]; si le nombre de bonus est de zero, resutl[0][0]==-1
* sinon resutl[0][0] contien la taille;
*/
/*
private int[][] processBonus()
{
checkInterruption();	// on doit tester l'interruption au début de chaque méthode


int k=0;
// on compte le nombre de bonus
k=getBoard().items.size();
// va contenir les coordonées des bonus x en [0][] r=et y en [1][]; une note sera attribué en [2][]
int result[][]= new int[3][k+1];
// si il n'y à pas de bonus
if(k==0)
{
result[0][0]=-1;
return result;
}else
{
result[0][0]=k+1;
}
k=1;



for (ItemInstance i: getBoard().items)
{
checkInterruption();	// une boucle, donc un autre test d'interruption
result[0][k]=i.x;
result[1][k]=i.y;

// on attribue une note selon le type de bonus
switch(i.type)
{	case OTHERS_FAST:
result[2][k]=-1;
break;
case OTHERS_REVERSE:
result[2][k]=-5;
break;
case OTHERS_THICK:
result[2][k]=-2;
break;
case OTHERS_SLOW:
result[2][k]=-4;
break;
case USER_FAST:
result[2][k]=-3;
break;
case USER_FLY:
result[2][k]=5;
break;
case USER_SLOW:
result[2][k]=1;
break;
}
// la note est modifier selon la distance entre les differents serpent et le bonus.



k++;
}

return result;

}
*/
/*
/**
* @param var true si on veut un bonus defensif false pour un offensif
* @return 1 si le meilleur bonus est à gauche, 2 s'il est à droite, 0 si la direction est bonne.
*/
/*
private int BonusDirection(boolean var)
{
int bonus[][]=processBonus();

if(bonus[0][0]==-1)
{
return 0;
}
int k=1;
// si on veu un bonus defensif
if (var)
{
int max=bonus[2][1];

// on choisi le meilleur bonus;
for(int i=1;i<bonus[0][0]-1;i++)
{
checkInterruption();	// une boucle, donc un autre test d'interruption
if(max<bonus[2][i])
{
max=bonus[2][i];
k=i;
}
}
}else
{
int min=bonus[2][1];

// on choisi le meilleur bonus;
for(int i=1;i<bonus[0][0]-1;i++)
{
checkInterruption();	// une boucle, donc un autre test d'interruption
if(min>bonus[2][i])
{
min=bonus[2][i];
k=i;
}
}
}
Position position = new Position(bonus[0][k],bonus[1][k]);
// on calcul l'angle entre la tete du de l'agent et le bonus
double angle = Math.atan2(position.y-agentSnake.currentY, position.x-agentSnake.currentX);
if(angle<0)
{
angle = angle + 2*Math.PI;	
}

if(angle==0)// le bonus est devant
{
return 0;
}

if(angle==Math.PI)// si le bonus est derrière (à amélioré)
{
return 1;
}

if(angle<Math.PI)// le bonus est à gauche
{
return 1;
}else
if (angle<2*Math.PI)// le bonus est à droite.
{
return 2;
}

return 0;
}
*/



