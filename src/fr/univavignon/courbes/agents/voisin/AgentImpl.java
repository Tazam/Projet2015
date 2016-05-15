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
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import fr.univavignon.courbes.agents.Agent;
import fr.univavignon.courbes.common.Board;
import fr.univavignon.courbes.common.Board.State;
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
	
	private long startTime = 200;
	private Direction direction;
	private int levelMax=5;
	private Set<Direction> prevent = new HashSet<Direction>();
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

	@Override
	public Direction processDirection() {
		
		checkInterruption();
		direction = Direction.NONE;

		Board board = getBoard();
		if(board == null)
			return Direction.NONE;
		else
		{
			long time=0;
			if( board.state==State.REGULAR)
				time = System.currentTimeMillis();
			agentSnake = board.snakes[getPlayerId()];
			updateAngles();
			Set<Position> trail = new TreeSet<Position>();
			Position posSnake = new Position(agentSnake.currentX,agentSnake.currentY);
			System.out.println("POSSNAKE="+posSnake);
			getObstacle(board, trail);
			System.out.println("debut");
			double val = bestChoice(board, trail,posSnake,0, false, currentAngle, posSnake, 0, posSnake);

			
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
			if( board.state==State.REGULAR&& trail.size()>1000)
				startTime=(System.currentTimeMillis()-time+startTime)/2;
			System.out.println("time="+startTime+" niveau max="+levelMax);
			System.out.println("tourne "+direction);	
			System.out.println("fin");		
			if(startTime>300 && board.state==State.REGULAR && trail.size()>1000 && levelMax>3)
				levelMax--;
			else if(startTime<100 && board.state==State.REGULAR && trail.size()>1000 && levelMax<8 && val>=levelMax-2)
				levelMax++;
			return direction;
		}
	}
	
	public int getWhereSnake(Position position, Position position2, int val, Set<Position>trail, Board board)
	{	
		checkInterruption();	// on doit tester l'interruption au début de chaque méthode
		
		//----------------------------------------------------------------------------------------------------------------------------------------------------------------------
		//on ne peut pas toucher sa propre tete
		for (Position mytrail : agentSnake.newTrail)
		{
			if((Math.sqrt((Math.pow((mytrail.x-position.x), 2))-(Math.pow((mytrail.y-position.y), 2)))<agentSnake.headRadius) && (Math.sqrt((Math.pow((mytrail.x-position2.x), 2))-(Math.pow((mytrail.y-position2.y), 2)))<agentSnake.headRadius))
			{
				return 0;
			}
			
		}
		
		
		if(position.x<0 || position.y<0 || position.x>board.width || position.y > board.height)
		{
			return 2;
		}
		//on créé l'angle pour placer le rectangle et le faire tourner plus tard
		double angle= Math.atan2(position.y - position2.y, position.x - position2.x);
		double angle2=angle+Math.PI/2;
		double a = agentSnake.headRadius*Math.cos(angle2);
		double b = agentSnake.headRadius*Math.sin(angle2);
		
		// on positionne les points du rectangle
		Position tmp[] = new Position[4];
		tmp[0]=new Position((int)(position.x+a),(int)(position.y+b));
		tmp[1]=new Position((int)(position.x-a),(int)(position.y-b));
		tmp[2]=new Position((int)(position2.x+a),(int)(position2.y+b));
		tmp[3]=new Position((int)(position2.x-a),(int)(position2.y-b));
		
		// on trouve son centre pour faire des rotations plus tard
		Position center = new Position((tmp[0].x+tmp[3].x)/2, (tmp[0].y+tmp[3].y)/2);
		
		// on fait tourner les points un à un
		int dx = tmp[0].x - center.x;
		int dy = tmp[0].y - center.y;
		double newX = center.x - dx*Math.cos(-angle) + dy*Math.sin(-angle);
		double newY = center.x - dx*Math.sin(-angle) - dy*Math.cos(-angle);
		tmp[0].x = (int)newX;
		tmp[0].y = (int)newY;
		
		dx = tmp[1].x - center.x;
		dy = tmp[1].y - center.y;
		newX = center.x - dx*Math.cos(-angle) + dy*Math.sin(-angle);
		newY = center.x - dx*Math.sin(-angle) - dy*Math.cos(-angle);
		tmp[1].x = (int)newX;
		tmp[1].y = (int)newY;
		
		dx = tmp[2].x - center.x;
		dy = tmp[2].y - center.y;
		newX = center.x - dx*Math.cos(-angle) + dy*Math.sin(-angle);
		newY = center.x - dx*Math.sin(-angle) - dy*Math.cos(-angle);
		tmp[2].x = (int)newX;
		tmp[2].y = (int)newY;
		
		dx = tmp[3].x - center.x;
		dy = tmp[3].y - center.y;
		newX = center.x - dx*Math.cos(-angle) + dy*Math.sin(-angle);
		newY = center.x - dx*Math.sin(-angle) - dy*Math.cos(-angle);
		tmp[3].x = (int)newX;
		tmp[3].y = (int)newY;
		
		// on récupère les deux coins extrèmes		
		Position corner1, corner2;
		int min=0, max=0;
		
		for(int i=0;i<tmp.length;i++){
			if(tmp[i].x < tmp[min].x && tmp[i].y < tmp[min].y){
				min = i;
			}
			if(tmp[i].x > tmp[max].x && tmp[i].y > tmp[max].y){
				max = 1;
			}
		}
		
		corner1 = new Position(tmp[min]);
		corner2 = new Position(tmp[max]);
		
		//----------------------------------------------------------------------------------------------------------------------------------------------------------------------
		
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
				
				//----------------------------------------------------------------------------------------------------------------------------------------------------------------------
				
				// on créé une nouvelle position pas copie de la trail, pour pouvoir lui faire une rotation sans modifier la variable pos
				Position trailPosTmp = new Position(pos);
				dx = trailPosTmp.x - center.x;
				dy = trailPosTmp.y - center.y;
				newX = center.x - dx*Math.cos(-angle) + dy*Math.sin(-angle);
				newY = center.x - dx*Math.sin(-angle) - dy*Math.cos(-angle);
				trailPosTmp.x = (int)newX;
				trailPosTmp.y = (int)newY;
				
				//----------------------------------------------------------------------------------------------------------------------------------------------------------------------
				
				checkInterruption();

				//----------------------------------------------------------------------------------------------------------------------------------------------------------------------
				
				//TODO verifier la formule
				//on vérifie que la trail est dans le rectangle
				if(trailPosTmp.x >= corner1.x && trailPosTmp.x <= corner2.x && trailPosTmp.y >= corner1.y && trailPosTmp.y <= corner2.y)
				{
					return 2;
				}
				
				//TODO  m'expliquer comment on arrive là ....
				if((position.x==s.currentX && position.y==s.currentY) && (position2.x==s.currentX && position2.y==s.currentY))
				{
					return 2;
				}
				
				//----------------------------------------------------------------------------------------------------------------------------------------------------------------------
				
			}
		}
		return 0;
	}

	private void updateAngles()
	{
		checkInterruption();
		// angle de déplacement
		currentAngle = agentSnake.currentAngle%(2*Math.PI);
		if(currentAngle<0)
			currentAngle+=2*Math.PI;
	}
	 
	/**
	 * @param board Le terrain
	 * @param trail La liste des obstacles
	 * @param pos La position testée
	 * @param val La valeur actuel du chemin
	 * @param danger vrai si on est passé prés de la tete d'un autre snake, faux sinon
	 * @param angle L'angle testé
	 * @param posSnake La position actuelle du snake
	 * @param niveau Le niveau de récursivité actuel
	 * @param lastpos La derniere pos testée
	 * @return
	 */
	public double bestChoice(Board board, Set<Position> trail, Position pos, double val, boolean danger, double angle, Position posSnake, int niveau, Position lastpos)
	{
		checkInterruption();
		HashMap<Direction, Double > valeurDirection = new HashMap<Direction, Double >();
		double resultat = val;
		if(pos!=posSnake)
		{
			if(niveau>=levelMax) // nombre de tour de boucle max
			{
				return resultat;
			}
			int res = getWhereSnake(pos, lastpos,10, trail , board);
			//on cherche a savoir ce qui riste de se passer si on arrive à cette position
			if(res==0)//safe
				if(danger)//on est deja passé prés d'une tete
					val+=0.5;
				else
					val++;
			if(res==1)//on passe prés d'une tête
			{
				val+=0.5;
				danger=true;
			}
			if(res==2)//on risque de mourir
			{
				if(niveau<=3)
				{
					double ok = startTime*agentSnake.movingSpeed*niveau;
					System.out.println("il y a un obstacle entre "+lastpos+" et "+pos+" distance parcourue depuis la snake = "+ok);
					
				}
				return resultat;
			}
		}
		Pair<Position, Double> cpos = new Pair<Position, Double>();
		cpos = calculatePosition(Direction.RIGHT, pos, angle);
		if(niveau<=1)
			System.out.println("test Position RIGHT niveau="+niveau+ "  position="+cpos.getFirst());
		valeurDirection.put(Direction.RIGHT, bestChoice(board, trail, cpos.getFirst(), val, danger, cpos.getSecond(),posSnake,niveau+1, pos));
		checkInterruption();
		cpos = calculatePosition(Direction.LEFT, pos, angle);
		if(niveau<=1)
			System.out.println("test Position LEFT niveau="+niveau+ "  position="+cpos.getFirst());
		valeurDirection.put(Direction.LEFT, bestChoice(board, trail, cpos.getFirst(), val, danger, cpos.getSecond(),posSnake,niveau+1, pos));
		checkInterruption();
		cpos = calculatePosition(Direction.NONE, pos, angle);
		if(niveau<=1)
			System.out.println("test Position NONE niveau="+niveau+ "  position="+cpos.getFirst());
		valeurDirection.put(Direction.NONE, bestChoice(board, trail, cpos.getFirst(), val, danger, cpos.getSecond(),posSnake,niveau+1, pos));
		checkInterruption();
		
		if(valeurDirection.get(Direction.RIGHT)>=valeurDirection.get(Direction.LEFT) && valeurDirection.get(Direction.RIGHT)>=valeurDirection.get(Direction.NONE))
		{
			resultat = valeurDirection.get(Direction.RIGHT);
			direction = Direction.RIGHT;
		}
		else if(valeurDirection.get(Direction.LEFT)>=valeurDirection.get(Direction.RIGHT) && valeurDirection.get(Direction.LEFT)>=valeurDirection.get(Direction.NONE))
		{
			resultat = valeurDirection.get(Direction.LEFT);
			direction = Direction.LEFT;
		}
		else
		{
			resultat = valeurDirection.get(Direction.NONE); 
			direction = Direction.NONE;
		}
		if(niveau==0)
		{
			System.out.println(valeurDirection);
			preventChoice(valeurDirection);
		}
		return resultat;
	}
	
	void preventChoice(HashMap<Direction, Double >  valeurDirection){
		double none=valeurDirection.get(Direction.NONE);
		double right=valeurDirection.get(Direction.RIGHT);
		double left=valeurDirection.get(Direction.LEFT);
		if(none<right-0.5 || none<left-0.5)
		{
			prevent.add(Direction.NONE);
		}
		if(left<right-0.5 || left<none-0.5)
		{
			prevent.add(Direction.LEFT);

		}
		if(right<none-0.5 || right<left-0.5)
		{
			prevent.add(Direction.RIGHT);
		}
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

