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
//import fr.univavignon.courbes.common.ItemInstance;
import fr.univavignon.courbes.common.Position;
import fr.univavignon.courbes.common.Snake;

/*
 * Fonction prenant en paramètre une position et un int et qui regarde si la position touche un snake adverse, renvoie 2
 * si jamais on est à une distance x (le int) on renvoie 1, sinon renvoie 0;
 */

/**
 * @author pierre quentin gatean maxime
 *
 */
public class AgentImpl extends Agent {
	/** Direction courante du serpent de l'agent */
	private double currentAngle;@SuppressWarnings("javadoc")
	/**
	 * temps nécessaire pour choisir une direction
	 */
	private long startTime = 200;
	/**
	 * direction choisie
	 */
	private Direction direction = Direction.NONE;
	private Direction lastDirection = Direction.NONE;
	/**
	 *  bordure
	 */
	private Set < Position > border = null;
	/**
	 * Nombre de récursivité maximum
	 */
	private int levelMax = 5;
	/**
	 * Liste des direction ou il ne faut pas aller
	 */
	private Set < Direction > prevent = new HashSet < Direction > ();
	/**
	 * Crée un agent contrôlant le joueur spécifié
	 * dans la partie courante.
	 * 
	 * @param playerId
	 * 		Numéro du joueur contrôlé par cet agent.
	 */
	public AgentImpl(Integer playerId) {
		super(playerId);
	}

	/** Serpent contrôlé par l'agent */
	private Snake agentSnake;

	@Override
	public Direction processDirection() {

		checkInterruption();
		direction = Direction.NONE;

		Board board = getBoard();
		if (board == null) return Direction.NONE;
		else {
			if (border == null) {
				border = new TreeSet < Position > ();
				getBorder(board);
			}
			long time = 0;
			if (board.state == State.REGULAR) time = System.currentTimeMillis();
			agentSnake = board.snakes[getPlayerId()];
			updateAngles();
			Set < Position > trail = new TreeSet < Position > (border);
			Position posSnake = new Position(agentSnake.currentX, agentSnake.currentY);
			System.out.println("POSSNAKE=" + posSnake);
			getObstacle(board, trail);

			double val = algoLocal(board, trail, posSnake, 0, currentAngle, posSnake, 0, posSnake);
			if (prevent.size() <= 1) {
				algoGlobal(board);
			}
			// si l'agent est sous le malus inverse on inverse ses choix de direction.
			if (agentSnake.inversion) {
				if (direction == Direction.RIGHT) {
					return Direction.LEFT;
				}

				if (direction == Direction.LEFT) {
					return Direction.RIGHT;
				}
			}
			if (board.state == State.REGULAR && trail.size() > 1000) startTime = (System.currentTimeMillis() - time + startTime) / 2;
			System.out.println("time=" + startTime + " niveau max=" + levelMax);
			System.out.println("tourne " + direction);
			if (startTime > 300 && board.state == State.REGULAR && trail.size() > 1000 && levelMax > 3) levelMax--;
			else if (startTime < 150 && board.state == State.REGULAR && trail.size() > 1000 && levelMax < 8 && val >= levelMax - 2) levelMax++;
			lastDirection = direction;
			return direction;
		}
	}

	/**
	 * @param position  une position du snake
	 * @param position2 une autre position du snake
	 * @param trail tableau des obstacles
	 * @param board le tableau de jeu
	 * @return vrai si il n'y a pas de danger entre les 2 positions, faux sinon
	 */
	public boolean isSafe(Position position, Position position2, Set < Position > trail, Board board) {
		checkInterruption(); // on doit tester l'interruption au début de chaque méthode

		//----------------------------------------------------------------------------------------------------------------------------------------------------------------------
		if (position.x < 0 || position.x > board.width || position.y < 0 || position.y > board.height) return false;
		//on créé l'angle pour placer le rectangle et le faire tourner plus tard
		double angle = Math.atan2(position.y - position2.y, position.x - position2.x);
		double angle2 = angle + Math.PI / 2;
		double a = agentSnake.headRadius * 2 * Math.cos(angle2);
		double b = agentSnake.headRadius * 2 * Math.sin(angle2);

		// on positionne les points du rectangle
		Position tmp[] = new Position[4];
		tmp[0] = new Position((int)(position.x + a), (int)(position.y + b));
		tmp[1] = new Position((int)(position.x - a), (int)(position.y - b));
		tmp[2] = new Position((int)(position2.x + a), (int)(position2.y + b));
		tmp[3] = new Position((int)(position2.x - a), (int)(position2.y - b));
		// on trouve son centre pour faire des rotations plus tard
		Position center = new Position((tmp[0].x + tmp[3].x) / 2, (tmp[0].y + tmp[3].y) / 2);

		// on fait tourner les points un à un
		int dx = tmp[0].x - center.x;
		int dy = tmp[0].y - center.y;
		double newX = center.x - dx * Math.cos(-angle) + dy * Math.sin(-angle);
		double newY = center.x - dx * Math.sin(-angle) - dy * Math.cos(-angle);
		tmp[0].x = (int) newX;
		tmp[0].y = (int) newY;

		dx = tmp[1].x - center.x;
		dy = tmp[1].y - center.y;
		newX = center.x - dx * Math.cos(-angle) + dy * Math.sin(-angle);
		newY = center.x - dx * Math.sin(-angle) - dy * Math.cos(-angle);
		tmp[1].x = (int) newX;
		tmp[1].y = (int) newY;

		dx = tmp[2].x - center.x;
		dy = tmp[2].y - center.y;
		newX = center.x - dx * Math.cos(-angle) + dy * Math.sin(-angle);
		newY = center.x - dx * Math.sin(-angle) - dy * Math.cos(-angle);
		tmp[2].x = (int) newX;
		tmp[2].y = (int) newY;

		dx = tmp[3].x - center.x;
		dy = tmp[3].y - center.y;
		newX = center.x - dx * Math.cos(-angle) + dy * Math.sin(-angle);
		newY = center.x - dx * Math.sin(-angle) - dy * Math.cos(-angle);
		tmp[3].x = (int) newX;
		tmp[3].y = (int) newY;

		// on récupère les deux coins extrèmes		
		Position corner1, corner2;
		int min = 0, max = 0;



		for (int i = 1; i < tmp.length; i++) {
			if (tmp[i].x <= tmp[min].x && tmp[i].y <= tmp[min].y) {
				min = i;
			}
			if (tmp[i].x >= tmp[max].x && tmp[i].y >= tmp[max].y) {
				max = i;
			}
		}

		corner1 = new Position(tmp[min]);
		corner2 = new Position(tmp[max]);

		//----------------------------------------------------------------------------------------------------------------------------------------------------------------------
		for (Position pos: trail) {
			//----------------------------------------------------------------------------------------------------------------------------------------------------------------------

			// on créé une nouvelle position pas copie de la trail, pour pouvoir lui faire une rotation sans modifier la variable pos
			Position trailPosTmp = new Position(pos);
			dx = trailPosTmp.x - center.x;
			dy = trailPosTmp.y - center.y;
			newX = center.x - dx * Math.cos(-angle) + dy * Math.sin(-angle);
			newY = center.x - dx * Math.sin(-angle) - dy * Math.cos(-angle);
			trailPosTmp.x = (int) newX;
			trailPosTmp.y = (int) newY;

			//----------------------------------------------------------------------------------------------------------------------------------------------------------------------
			checkInterruption();

			//----------------------------------------------------------------------------------------------------------------------------------------------------------------------


			//on vérifie que la trail est dans le rectangle
			if (trailPosTmp.x >= corner1.x && trailPosTmp.x <= corner2.x && trailPosTmp.y >= corner1.y && trailPosTmp.y <= corner2.y) {
				double distance = Math.sqrt(Math.pow(agentSnake.currentX - pos.x, 2) + Math.pow(agentSnake.currentY - pos.y, 2));
				if (distance > agentSnake.headRadius) {
					return false;
				}
			}
			//----------------------------------------------------------------------------------------------------------------------------------------------------------------------
		}
		return true;
	}

	/**
	 * Met a jour l'angle du snake
	 */
	private void updateAngles() {
		checkInterruption();
		// angle de déplacement
		currentAngle = agentSnake.currentAngle % (2 * Math.PI);
		if (currentAngle < 0) currentAngle += 2 * Math.PI;
	}

	public void algoGlobal(Board board) {
		boolean defense=false;
		
		//if(prevent.size()!=0)
		//	defense=true;
		int val = BonusDirection(defense, board);
				
		HashMap < Direction, Double > valeurDirection = new HashMap < Direction, Double > ();
		if(prevent.contains(Direction.NONE))
			valeurDirection.put(Direction.NONE, -100.0);
		else
			valeurDirection.put(Direction.NONE, 0.0);	
		if(prevent.contains(Direction.LEFT))
			valeurDirection.put(Direction.LEFT, -100.0);
		else
			valeurDirection.put(Direction.LEFT, 0.0);
		if(prevent.contains(Direction.RIGHT))
			valeurDirection.put(Direction.RIGHT, -100.0);
		else
			valeurDirection.put(Direction.RIGHT, 0.0);
		
		
		
		if(val==0 && !prevent.contains(Direction.NONE))
			direction = Direction.NONE;
		else if(val==1 && !prevent.contains(Direction.LEFT))
			direction = Direction.LEFT;
		else if (val==2 && !prevent.contains(Direction.RIGHT))
			direction = Direction.RIGHT;
	}
	/**
	 * @param board Le terrain
	 * @param trail La liste des obstacles
	 * @param pos La position testée
	 * @param val La valeur actuel du chemin
	 * @param angle L'angle testé
	 * @param posSnake La position actuelle du snake
	 * @param niveau Le niveau de récursivité actuel
	 * @param lastpos La derniere pos testée
	 * @return une valeur en fonction du niveau de récusivité et si la position est safe ou non
	 */
	public double algoLocal(Board board, Set < Position > trail, Position pos, double val, double angle, Position posSnake, int niveau, Position lastpos) {
		checkInterruption();
		HashMap < Direction, Double > valeurDirection = new HashMap < Direction, Double > ();
		double resultat = val;
		if (niveau >= levelMax) // nombre de tour de boucle max
		{
			return resultat;
		}
		if (niveau > 1) {
			//on cherche a savoir ce qui riste de se passer si on arrive à cette position
			if (isSafe(pos, lastpos, trail, board)) //safe
			{
				resultat++;
			} else //on risque de mourir
			{
				return resultat - 1;
			}
		}

		Pair < Position, Double > cpos = new Pair < Position, Double > ();
		if (niveau > 1 || lastDirection == Direction.RIGHT) {
			cpos = calculatePosition(Direction.RIGHT, pos, angle);
			valeurDirection.put(Direction.RIGHT, algoLocal(board, trail, cpos.getFirst(), resultat, cpos.getSecond(), posSnake, niveau + 1, pos));
			if (lastDirection == Direction.RIGHT) valeurDirection.put(Direction.RIGHT, valeurDirection.get(Direction.RIGHT) + 2 / levelMax);
		}
		if (niveau > 1 || lastDirection == Direction.LEFT) {
			cpos = calculatePosition(Direction.LEFT, pos, angle);
			valeurDirection.put(Direction.LEFT, algoLocal(board, trail, cpos.getFirst(), resultat, cpos.getSecond(), posSnake, niveau + 1, pos));
			if (lastDirection == Direction.LEFT) valeurDirection.put(Direction.LEFT, valeurDirection.get(Direction.LEFT) + 2 / levelMax);
		}
		if (niveau > 1 || lastDirection == Direction.NONE) {
			cpos = calculatePosition(Direction.NONE, pos, angle);
			valeurDirection.put(Direction.NONE, algoLocal(board, trail, cpos.getFirst(), resultat, cpos.getSecond(), posSnake, niveau + 1, pos));
			if (lastDirection == Direction.NONE) valeurDirection.put(Direction.NONE, valeurDirection.get(Direction.NONE) + 2 / levelMax);
		}
		if (niveau <= 1) {
			return valeurDirection.get(lastDirection);
		}
		//prio droite
		if (valeurDirection.get(Direction.RIGHT) >= valeurDirection.get(Direction.LEFT) && valeurDirection.get(Direction.RIGHT) >= valeurDirection.get(Direction.NONE)) {
			resultat = valeurDirection.get(Direction.RIGHT);
			direction = Direction.RIGHT;
		} else if (valeurDirection.get(Direction.LEFT) >= valeurDirection.get(Direction.RIGHT) && valeurDirection.get(Direction.LEFT) >= valeurDirection.get(Direction.NONE)) {
			resultat = valeurDirection.get(Direction.LEFT);
			direction = Direction.LEFT;
		} else {
			resultat = valeurDirection.get(Direction.NONE);
			direction = Direction.NONE;
		}
		if (niveau == 2) {
			preventChoice(valeurDirection);
			System.out.println(valeurDirection);
		}
		return resultat;
	}

	/**
	 * @param valeurDirection les directions associées a une valeur
	 */
	void preventChoice(HashMap < Direction, Double > valeurDirection) {
		prevent.clear();
		double none = valeurDirection.get(Direction.NONE);
		double right = valeurDirection.get(Direction.RIGHT);
		double left = valeurDirection.get(Direction.LEFT);
		if (none < right - 0.5 || none < left - 0.5) {
			prevent.add(Direction.NONE);
		}
		if (left < right - 0.5 || left < none - 0.5) {
			prevent.add(Direction.LEFT);

		}
		if (right < none - 0.5 || right < left - 0.5) {
			prevent.add(Direction.RIGHT);
		}
		System.out.println(prevent);
	}


	/**
	 * @param board le terrain de jeu
	 * @param trail la liste des obstacles
	 */
	public void getObstacle(Board board, Set < Position > trail) {
		checkInterruption();
		for (int i = 0; i < board.snakes.length; ++i) {
			Snake snake = board.snakes[i];
			trail.addAll(snake.oldTrail);
			trail.addAll(snake.newTrail);
		}
	}


	/**
	 * @param d direction choisie
	 * @param p position choisie
	 * @param angle angle choisie
	 * @return nouvelle position trouvé en fonction des 3 parametres
	 */
	public Pair < Position, Double > calculatePosition(Direction d, Position p, double angle) {
		float realTime = startTime;
		if (realTime < 100) realTime = 100;
		float dist = realTime * agentSnake.movingSpeed;
		float delta = realTime * agentSnake.turningSpeed * d.value;
		angle = (float)((angle + delta + 2 * Math.PI) % (2 * Math.PI));
		// conversion de polaire vers cartésien
		double tempX = dist * Math.cos(angle);
		double tempY = dist * Math.sin(angle);

		// translation vers les coordonnées réelles de l'aire de jeu
		float realX = p.x + (float) tempX;
		float realY = p.y + (float) tempY;
		Position result = new Position((int) Math.round(realX), (int) Math.round(realY));

		Pair < Position, Double > pair = new Pair < Position, Double > (result, angle);

		return pair;
	}

	public void getBorder(Board board) {
		for (int i = 0; i < board.width; i++) {
			Position pos1 = new Position(i, 0);
			Position pos2 = new Position(i, board.height);
			border.add(pos1);
			border.add(pos2);
		}
		for (int i = 0; i < board.height; i++) {
			Position pos1 = new Position(0, i);
			Position pos2 = new Position(board.width - 0, i);
			border.add(pos1);
			border.add(pos2);
		}
	}


	/**
	 * Choisi un bonus.
	 * @param var contient un int qui influ sur la note suivant la situation du snake.
	 * @return un tableau d'int qui va contenir les coordonées des bonus x en [0][] r=et y en [1][]; si le nombre de bonus est de zero, resutl[0][0]==-1
	 * sinon resutl[0][0] contien la taille;
	 */

	/**
	 * @param board plateau de jeu
	 * @return un tableau d'int qui va contenir les coordonées des bonus x en [0][] r=et y en [1][]; si le nombre de bonus est de zero, resutl[0][0]==-1
	 * sinon resutl[0][0] contien la taille;
	 */
	private int[][] processBonus(Board board) {
		checkInterruption(); // on doit tester l'interruption au début de chaque méthode

		int k = 0;
		// on compte le nombre de bonus
		k = board.items.size();
		// va contenir les coordonées des bonus x en [0][] r=et y en [1][]; une note sera attribué en [2][]
		int result[][] = new int[3][k + 1];
		// si il n'y à pas de bonus
		if (k == 0) {
			result[0][0] = -1;
			return result;
		} else {
			result[0][0] = k + 1;
		}
		k = 1;
		for (ItemInstance i: board.items) {
			checkInterruption(); // une boucle, donc un autre test d'interruption
			result[0][k] = i.x;
			result[1][k] = i.y;

			// on attribue une note selon le type de bonus
			switch (i.type) {
			case OTHERS_FAST:
				result[2][k] = -1;
				break;
			case OTHERS_REVERSE:
				result[2][k] = -5;
				break;
			case OTHERS_THICK:
				result[2][k] = -2;
				break;
			case OTHERS_SLOW:
				result[2][k] = -4;
				break;
			case USER_FAST:
				result[2][k] = -3;
				break;
			case USER_FLY:
				result[2][k] = 5;
				break;
			case USER_SLOW:
				result[2][k] = 1;
				break;
			}
			// la note est modifier selon la distance entre les differents serpent et le bonus.


			k++;
		}

		return result;

	}

	/*
/**
* 
* 
*/
	/**
	 * @param var true si on veut un bonus defensif false pour un offensif
	 * @param board plateau de jeu
	 * @return 1 si le meilleur bonus est à gauche, 2 s'il est à droite, 0 si la direction est bonne, -1 si il n'y a pas de bon choix.
	 */
	private int BonusDirection(boolean	var, Board board) {
		int bonus[][] = processBonus(board);

		if (bonus[0][0] == -1) {
			return -1;
		}
		int k = 1;
		// si on veut un bonus defensif
		if (var) {
			int max = bonus[2][1];

			// on choisi le meilleur bonus;
			for (int i = 1; i < bonus[0][0] - 1; i++) {
				checkInterruption(); // une boucle, donc un autre test d'interruption
				if (max < bonus[2][i]) {
					max = bonus[2][i];
					k = i;
				}
			}
		} else {
			int min = bonus[2][1];

			// on choisi le meilleur bonus;
			for (int i = 1; i < bonus[0][0] - 1; i++) {
				checkInterruption(); // une boucle, donc un autre test d'interruption
				if (min > bonus[2][i]) {
					min = bonus[2][i];
					k = i;
				}
			}
		}
		Position position = new Position(bonus[0][k], bonus[1][k]);
		// on calcul l'angle entre la tete du de l'agent et le bonus
		double angle = Math.atan2(position.y - agentSnake.currentY, position.x - agentSnake.currentX);
		if (angle < 0) {
			angle = angle + 2 * Math.PI;
		}

		if (angle == 0) // le bonus est devant
		{
			return 0;
		}

		if (angle == Math.PI) // si le bonus est derrière (à amélioré)
		{
			return 1;
		}

		if (angle < Math.PI) // le bonus est à gauche
		{
			return 1;
		} else if (angle < 2 * Math.PI) // le bonus est à droite.
		{
			return 2;
		}

		return 0;
	}

}