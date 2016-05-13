
/*
 * 
 * TODO 
 * ajouter une fonction/ ameliorer la fonction getWhereSnake
 * La fonction devra prendre 2 poisitions en parametre et verifier qu'il n'y a pas d'obstacle entre les 2 pos
 * (return 2= on passe par un obstacle,  return 1= on s'approche trop pres de la tete d'un serpent adverse, return 0 on est safe)
 * 
 * TODO 
 * optimiser le programme : voici quelque valeur de test 
 *  val max		   		1	   2	   3	   4	   5	   6	    7   	8	    9
 *	tour de boucle		4	   4	   120	   350	   1000	   3000   	6500    27000	40000
 *	temps(ms)			1	   1	   15	   70	   200	   600	    1500    3500	4500
 *	temps/boucle		0,25   0,25	   0,125   0,2	   0,2	   0,2   	0,2    	0,13	0,11
 *	distance (pixel)	1	   1	   4	   30	   120	   400   	500  	650 	800
 *
 *  Il faudrait obtenir :
 *	  temps(ms)	      	500 ou moins
 *    distance (pixel)	750 ou plus
 *
 *  TODO
 *  
 *  Reflechir a un algo qui prend moins de ressource si jamais on n'arrive pas a faire fonctionner l'ia "voisin"
 *  
 *  
 *  Pour l'instant l'ia tourne en rond a cause du 1er todo (les position tester ne sont pas pile sur l'obstacle mais un peux apres)
 */

package fr.univavignon.courbes.agents.voisin;

/*
 * Courbes
 * Copyright 2015-16 L3 Info UAPV 2015-16
 * 
 * This file is part of Courbes.
 * 
 * Courbes is free software: you can redistribute it and/or modify it under the terms 
 * of the GNU General Public License as published by the Free Software Foundation, 
 * either version 2 of the License, or (at your option) any later version.
 * 
 * Courbes is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
 * PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Courbes. If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import fr.univavignon.courbes.agents.Agent;
import fr.univavignon.courbes.common.Board;
import fr.univavignon.courbes.common.Direction;
import fr.univavignon.courbes.common.ItemInstance;
import fr.univavignon.courbes.common.Position;
import fr.univavignon.courbes.common.Snake;

/*
 * Fonction prenant en paramètre une position et un int et qui regarde si la position touche un snake adverse, renvoie 2
 * si jamais on est à une distance x (le int) on renvoie 1, sinon renvoie 0;
 */

public class AgentImpl extends Agent
{
	/** Moitié de l'angle de vision de l'agent, i.e. délimitant la zone traitée devant lui pour détecter des obstacles. Contrainte : doit être inférieure à PI */
	private static double ANGLE_WIDTH = Math.PI/2;
	/** Distance en pixels à partir de laquelle on considère qu'on est dans un coin */
	private static int CORNER_THRESHOLD = 100;
	/** Direction courante du serpent de l'agent */
	private double currentAngle;
	
	private long startTime = 1000;
	private Direction direction;
	/**
	 * Crée un agent contrôlant le joueur spécifié
	 * dans la partie courante.
	 * 
	 * @param playerId
	 * 		Numéro du joueur contrôlé par cet agent.
	 */
	public AgentImpl(Integer playerId) 
	{	super(playerId);
	}

	/** Serpent contrôlé par l'agent */
	private Snake agentSnake;


	public int getWhereSnake(Position position, Position position2, int val, Set<Position>trail, Board board)
	{	
		checkInterruption();	// on doit tester l'interruption au début de chaque méthode
		for(Snake s: board.snakes)
		{
			checkInterruption();
			//Check si le serpent testé n'est pas l'agent.
			if(s.playerId!=agentSnake.playerId)
			{
				//si on est sur lui, on renvoie 2
				if((position.x==s.currentX && position.y==s.currentY) && (position2.x==s.currentX && position2.y==s.currentY))
				{
					return 2;
				}
				//si on est dans son rayon d'action (prédeterminé)
				if((Math.sqrt((Math.pow((s.currentX-position.x), 2))-(Math.pow((s.currentY-position.y), 2)))<val) && (Math.sqrt((Math.pow((s.currentX-position2.x), 2))-(Math.pow((s.currentY-position2.y), 2)))<val))
				{
					return 1;
				}
			}
			for(Position pos: trail)
			{
				checkInterruption();
				if((position.x==s.currentX && position.y==s.currentY) && (position2.x==s.currentX && position2.y==s.currentY))
				{
					return 2;
				}
			}
		}
		return 0;
	}

	@Override
	public Direction processDirection() {
		
		checkInterruption();
		long time = System.currentTimeMillis();
		Board board = getBoard();
		if(board == null)
			return null;
		else
		{
			if(agentSnake==null)
				agentSnake = board.snakes[getPlayerId()];
			updateAngles();
			Set<Position> trail = new TreeSet<Position>();
			Position posSnake = new Position(agentSnake.currentX,agentSnake.currentY);
			getObstacle(board, trail);
			System.out.println("debut");
			int niveau=0;
			System.out.println("ma pos="+posSnake);
			bestChoice(board, trail,posSnake,0, false, currentAngle, posSnake, niveau, posSnake);
			startTime=(System.currentTimeMillis()-time+startTime)/2;
			System.out.println(startTime);
			System.out.println("fin");
			
			// si l'agent est sous le malus inverse on inverse ses choix de direction.
			if(agentSnake.inversion)
			{
				if(direction==Direction.RIGHT)
				{
					return Direction.LEFT;
				}
				
				if(direction==Direction.LEFT)
				{
					return Direction.RIGHT;
				}
			}
			
			return direction;
		}
	}
	

	
	private void updateAngles()
	{
		checkInterruption();
		// angle de déplacement
		currentAngle = agentSnake.currentAngle%(2*Math.PI);
		if(currentAngle<0)
			currentAngle+=2*Math.PI;
	}
	 
	public double bestChoice(Board board, Set<Position> trail, Position pos, double val, boolean danger, double angle, Position posSnake, int niveau, Position lastpos)
	{
		checkInterruption();
		HashMap<Direction, Double > valeurDirection = new HashMap<Direction, Double >();
		double resultat = val;
		if(pos!=posSnake)
		{
			return resultat;
		}
		if(niveau>=6) // nombre de tour de boucle max
		{
			return resultat;
		}
		int res = getWhereSnake(pos, lastpos,10, trail , board);
		if(res==0)
			if(danger)
				val+=0.5;
			else
				val++;
		if(res==1)
		{
			val+=0.5;
			danger=true;
		}
		if(res==2)
		{
			return resultat;
		}
		Pair<Position, Double> cpos = new Pair<Position, Double>();
		cpos = calculatePosition(Direction.RIGHT, pos, angle);
		valeurDirection.put(Direction.RIGHT, bestChoice(board, trail, cpos.getFirst(), val, danger, cpos.getSecond(),posSnake,niveau+1, pos));
		checkInterruption();
		cpos = calculatePosition(Direction.LEFT, pos, angle);
		valeurDirection.put(Direction.LEFT, bestChoice(board, trail, cpos.getFirst(), val, danger, cpos.getSecond(),posSnake,niveau+1, pos));
		checkInterruption();
		cpos = calculatePosition(Direction.NONE, pos, angle);
		valeurDirection.put(Direction.NONE, bestChoice(board, trail, cpos.getFirst(), val, danger, cpos.getSecond(),posSnake,niveau+1, pos));
		checkInterruption();
		double dist = Math.sqrt(
				Math.pow(pos.x-cpos.getFirst().x, 2) 
				+ Math.pow(pos.y-cpos.getFirst().y,2));
		if(valeurDirection.get(Direction.RIGHT)>=valeurDirection.get(Direction.LEFT) && valeurDirection.get(Direction.RIGHT)>=valeurDirection.get(Direction.NONE))
		{
			resultat = valeurDirection.get(Direction.RIGHT);
			if(niveau<=2)
			System.out.println("niveau="+niveau+" pos="+pos+ " cpos="+cpos.getFirst()+ "dist="+dist);
			direction = Direction.RIGHT;
		}
		else if(valeurDirection.get(Direction.LEFT)>=valeurDirection.get(Direction.RIGHT) && valeurDirection.get(Direction.LEFT)>=valeurDirection.get(Direction.NONE))
		{
			resultat = valeurDirection.get(Direction.LEFT);
			if(niveau<=2)
			System.out.println("niveau="+niveau+" pos="+pos+ " cpos="+cpos.getFirst()+ "dist="+dist);
			direction = Direction.LEFT;
		}
		else
		{
			resultat = valeurDirection.get(Direction.NONE); 
			if(niveau<=2)
			System.out.println("niveau="+niveau+" pos="+pos+ " cpos="+cpos.getFirst()+ "dist="+dist);
			direction = Direction.NONE;
		}
		if(niveau==0)
			System.out.println(valeurDirection);
			
		return resultat;
	}
	
	private boolean isInCorner(Position position, Board board)
	{
		checkInterruption();	// on doit tester l'interruption au début de chaque méthode
		boolean result = position.x<CORNER_THRESHOLD && position.y<CORNER_THRESHOLD
			|| board.width-position.x<CORNER_THRESHOLD && position.y<CORNER_THRESHOLD
			|| position.x<CORNER_THRESHOLD && board.height-position.y<CORNER_THRESHOLD
			|| board.width-position.x<CORNER_THRESHOLD && board.height-position.y<CORNER_THRESHOLD;
		return result;
	}
	
	
	public void getObstacle(Board board, Set<Position> trail)
	{		
		checkInterruption();
		for(int i=0;i<board.snakes.length;++i)
		{	
			Snake snake = board.snakes[i];	
			trail.addAll(snake.oldTrail);
			trail.addAll(snake.newTrail);
		}
	}
	
	
	public Pair<Position, Double> calculatePosition(Direction d, Position p, double angle){
		checkInterruption();
		double finalAngle = angle*Math.PI/180;
		float distance = startTime*agentSnake.movingSpeed;
		
		if(d == Direction.LEFT )
			finalAngle = finalAngle + (agentSnake.turningSpeed*startTime);
		else if(d == Direction.RIGHT)
			finalAngle = finalAngle - (agentSnake.turningSpeed*startTime);
		
		double x = distance*Math.cos(finalAngle);
		double y = distance*Math.sin(finalAngle);
		
		Position finalPosition = new Position((int)Math.round(p.x+x), (int)Math.round(p.y+y));
		
		Pair<Position, Double> pair = new Pair<Position, Double>(finalPosition, angle);
		
		return pair;
	}
	
////////////////////////////////////////////////////////////////
////TRAITEMENT DES BONUS
////////////////////////////////////////////////////////////////
/**
* Choisi un bonus.
* @param var contient un int qui influ sur la note suivant la situation du snake.
* @return un tableau d'int qui va contenir les coordonées des bonus x en [0][] r=et y en [1][]; si le nombre de bonus est de zero, resutl[0][0]==-1
* sinon resutl[0][0] contien la taille;
*/

private int[][] processBonus()
{
	checkInterruption();	// on doit tester l'interruption au début de chaque méthode


	int k=0;
	// on compte le nombre de bonus
	for (ItemInstance i: getBoard().items)
	{
		checkInterruption();	// une boucle, donc un autre test d'interruption
		k++;
	}
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

/**
 * @param var true si on veut un bonus defensif false pour un offensif
 * @return 1 si le meilleur bonus est à gauche, 2 s'il est à droite, 0 si la direction est bonne.
 */
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








}

