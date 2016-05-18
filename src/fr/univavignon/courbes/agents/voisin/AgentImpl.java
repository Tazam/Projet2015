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
	private int repeat=2;
	/**
	 * temps nécessaire pour choisir une direction
	 */
	private long startTime = 250;
	/**
	 * temps minimum pour une  decision locale
	 */
	private int timeMin = 150;
	/**
	 * temps maximum pour une  decision locale
	 */
	private int timeMax = 350;
	/**
	 * direction choisie
	 */
	private int CORNER_THRESHOLD = 150;
	/**
	 * direction choisie
	 */
	private Direction direction = Direction.NONE;
	/**
	 * derniere direction choisie
	 */
	private Direction lastDirection = Direction.NONE;
	/**
	 *  bordure
	 */
	private Set < Position > border = null;
	/**
	 * Nombre de récursivité maximum
	 */
	private int levelMax = 6;
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
			getObstacle(board, trail);

			double val = algoLocal(board, trail, posSnake, 0, currentAngle, posSnake, 0, posSnake);
			if (board.state == State.REGULAR && trail.size() > 1000) startTime = (System.currentTimeMillis() - time + startTime) / 2;
			
			algoGlobal(board, trail);
			
			// si l'agent est sous le malus inverse on inverse ses choix de direction.
			if (agentSnake.inversion) {
				if (direction == Direction.RIGHT) {
					return Direction.LEFT;
				}

				if (direction == Direction.LEFT) {
					return Direction.RIGHT;
				}
			}
			lastDirection = direction;
			if (startTime > timeMax && board.state == State.REGULAR && trail.size() > 1000 && levelMax > 5) levelMax--;
			else if (startTime < timeMin && board.state == State.REGULAR && trail.size() > 1000 && levelMax < 10 && val >= levelMax - 2) levelMax++;
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
		if (position.x < 10 || position.x > board.width - 10 || position.y < 10 || position.y > board.height - 10) return false;
		//on créé l'angle pour placer le rectangle et le faire tourner plus tard
		double angle = Math.atan2(position.y - position2.y, position.x - position2.x);
		double angle2 = angle + Math.PI / 2;
		double a = agentSnake.headRadius * 1.5 * Math.cos(angle2);
		double b = agentSnake.headRadius * 1.5 * Math.sin(angle2);

		// on positionne les points du rectangle
		Position tmp[] = new Position[4];
		tmp[0] = new Position((int)(position.x + a), (int)(position.y + b));
		tmp[1] = new Position((int)(position.x - a), (int)(position.y - b));
		tmp[2] = new Position((int)(position2.x + a), (int)(position2.y + b));
		tmp[3] = new Position((int)(position2.x - a), (int)(position2.y - b));
		// on trouve son centre pour faire des rotations plus tard
		Position center = new Position((tmp[0].x + tmp[1].x + tmp[2].x + tmp[3].x) / 4, (tmp[0].y + tmp[1].y + tmp[2].y + tmp[3].y) / 4);

		// on fait tourner les points un à un
		int dx = tmp[0].x - center.x;
		int dy = tmp[0].y - center.y;
		double newX = dx * Math.cos(-angle) - dy * Math.sin(-angle) + center.x;
		double newY = dx * Math.sin(-angle) + dy * Math.cos(-angle) + center.y;
		tmp[0].x = (int) newX;
		tmp[0].y = (int) newY;

		dx = tmp[1].x - center.x;
		dy = tmp[1].y - center.y;
		newX = dx * Math.cos(-angle) - dy * Math.sin(-angle) + center.x;
		newY = dx * Math.sin(-angle) + dy * Math.cos(-angle) + center.y;
		tmp[1].x = (int) newX;
		tmp[1].y = (int) newY;

		dx = tmp[2].x - center.x;
		dy = tmp[2].y - center.y;
		newX = dx * Math.cos(-angle) - dy * Math.sin(-angle) + center.x;
		newY = dx * Math.sin(-angle) + dy * Math.cos(-angle) + center.y;
		tmp[2].x = (int) newX;
		tmp[2].y = (int) newY;

		dx = tmp[3].x - center.x;
		dy = tmp[3].y - center.y;
		newX = dx * Math.cos(-angle) - dy * Math.sin(-angle) + center.x;
		newY = dx * Math.sin(-angle) + dy * Math.cos(-angle) + center.y;
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
			newX = dx * Math.cos(-angle) - dy * Math.sin(-angle) + center.x;
			newY = dx * Math.sin(-angle) + dy * Math.cos(-angle) + center.y;
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
		currentAngle = modulo(agentSnake.currentAngle, 2 * Math.PI);
	}
	
	/**
	 * @param a le nombre a moduler
	 * @param modulo la valeur du modulo
	 * @return le modulo de la valeur 'a'
	 */
	private double modulo(double a , double modulo){
		checkInterruption();
		// angle de déplacement
		double tmp;
		tmp = a % (modulo);
		if (tmp < 0) tmp += 2 * Math.PI;
		return tmp;
		
		
	}
	/**
	 * @param board terrain de jeu
	 * @param trail liste des obstacles
	 */
	public void algoGlobal(Board board, Set < Position > trail) {

		//gestion des coins
		if (isInCorner(board)) {
			if (lastDirection != Direction.NONE) direction = lastDirection;
			else prevent.add(Direction.NONE);
		} else {
			HashMap < Direction, Double > valeurDirection = new HashMap < Direction, Double > ();
			valeurDirection.put(Direction.NONE, 0.0);
			valeurDirection.put(Direction.LEFT, 0.0);
			valeurDirection.put(Direction.RIGHT, 0.0);
			double distMinNone=-1;
			double distMinLeft=-1;
			double distMinRight=-1;
			//si on va vers un mur non  detecté par l'algo local
				for (Position obstacle: trail) {
					
					double angle = modulo(Math.atan2(obstacle.y-agentSnake.currentY,obstacle.x-agentSnake.currentX),2*Math.PI);
					double angletmp=modulo(angle - currentAngle,2*Math.PI);

					double distance = Math.sqrt(Math.pow(agentSnake.currentX - obstacle.x, 2) + Math.pow(agentSnake.currentY - obstacle.y, 2));
						if (angletmp>=3*Math.PI/2 || angletmp<=Math.PI/2) {
							if (distance>agentSnake.headRadius && (distance< 50 || (lastDirection==Direction.LEFT && distance<75)) && angletmp>=3*Math.PI/2 + 0.175) {
								if(distMinLeft==-1 || distance<distMinLeft)
									distMinLeft=distance;
								valeurDirection.put(Direction.LEFT, -200.0);
							} else if (distance>agentSnake.headRadius && (distance< 50 || (lastDirection==Direction.RIGHT && distance<75)) && angletmp<=Math.PI/2 - 0.175) {
								if(distMinRight==-1 || distance<distMinRight)
									distMinRight=distance;
								valeurDirection.put(Direction.RIGHT, -200.0);
							} else if (distance>agentSnake.headRadius && (distance< 50 || (lastDirection==Direction.NONE && distance<75))) {
								if(distMinNone==-1 || distance<distMinNone)
									distMinNone=distance;
								valeurDirection.put(Direction.NONE, -200.0);
							}
						}
				}
				//si on ne trouve aucune issue, on va quand même à l'obstacle le plus loin
			if(valeurDirection.get(Direction.NONE)==-200 && valeurDirection.get(Direction.LEFT)==-200 && valeurDirection.get(Direction.RIGHT)==-200 && (prevent.size()!=0 || prevent.size()!=3))
			{
				if(distMinRight>=distMinNone && distMinRight>=distMinLeft)
					valeurDirection.put(Direction.RIGHT, -100.0);
				else if(distMinLeft>=distMinNone)
					valeurDirection.put(Direction.LEFT, -100.0);
				else
					valeurDirection.put(Direction.NONE, -100.0);
			}
			if (prevent.contains(Direction.NONE)) valeurDirection.put(Direction.NONE, valeurDirection.get(Direction.NONE)-100.0); 
			if (prevent.contains(Direction.LEFT)) valeurDirection.put(Direction.LEFT, valeurDirection.get(Direction.LEFT)-100.0);
			if (prevent.contains(Direction.RIGHT)) valeurDirection.put(Direction.RIGHT,valeurDirection.get(Direction.RIGHT)-100.0); 
			valeurDirection.put(lastDirection, valeurDirection.get(lastDirection) + 0.5);
			HashMap < Direction, Double > valeurSnake = new HashMap < Direction, Double > ();
			valeurSnake = getWhereSnakes(board);
			if(board.state==State.ENTRANCE)
			{
				valeurSnake.put(Direction.NONE,valeurSnake.get(Direction.NONE)*-1);
				valeurSnake.put(Direction.RIGHT,valeurSnake.get(Direction.RIGHT)*-1);
				valeurSnake.put(Direction.LEFT,valeurSnake.get(Direction.LEFT)*-1);
			}
			HashMap < Direction, Double > valeurObstacle = new HashMap < Direction, Double > ();
			valeurObstacle = getWhereObstacles(trail);
			HashMap < Direction, Double > valeurItem = new HashMap < Direction, Double > ();
			valeurItem = getWhereBonus(board);
			valeurDirection.put(Direction.NONE, valeurDirection.get(Direction.NONE) + valeurSnake.get(Direction.NONE) + valeurObstacle.get(Direction.NONE) + valeurItem.get(Direction.NONE));
			valeurDirection.put(Direction.LEFT, valeurDirection.get(Direction.LEFT) + valeurSnake.get(Direction.LEFT)+ valeurObstacle.get(Direction.LEFT) + valeurItem.get(Direction.LEFT));
			valeurDirection.put(Direction.RIGHT, valeurDirection.get(Direction.RIGHT) + valeurSnake.get(Direction.RIGHT)+ valeurObstacle.get(Direction.RIGHT) + valeurItem.get(Direction.RIGHT));
			

			if (valeurDirection.get(Direction.RIGHT) >= valeurDirection.get(Direction.LEFT) && valeurDirection.get(Direction.RIGHT) >= valeurDirection.get(Direction.NONE)) {
				direction = Direction.RIGHT;
			} else if (valeurDirection.get(Direction.LEFT) >= valeurDirection.get(Direction.RIGHT) && valeurDirection.get(Direction.LEFT) >= valeurDirection.get(Direction.NONE)) {
				direction = Direction.LEFT;
			} else {
				direction = Direction.NONE;
			}
		}
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
		if (niveau > repeat) {
			//on cherche a savoir ce qui riste de se passer si on arrive à cette position
			if (isSafe(pos, lastpos, trail, board)) //safe
			{
				resultat++;
			} else //on risque de mourir
			{
				return resultat-1;
			}
		}

		Pair < Position, Double > cpos = new Pair < Position, Double > ();
		if (niveau > repeat || lastDirection == Direction.RIGHT) {
			cpos = calculatePosition(Direction.RIGHT, pos, angle);
			valeurDirection.put(Direction.RIGHT, algoLocal(board, trail, cpos.getFirst(), resultat, cpos.getSecond(), posSnake, niveau + 1, pos));
			if (lastDirection == Direction.RIGHT) valeurDirection.put(Direction.RIGHT, valeurDirection.get(Direction.RIGHT) + 2 / levelMax);
		}
		if (niveau > repeat || lastDirection == Direction.LEFT) {
			cpos = calculatePosition(Direction.LEFT, pos, angle);
			valeurDirection.put(Direction.LEFT, algoLocal(board, trail, cpos.getFirst(), resultat, cpos.getSecond(), posSnake, niveau + 1, pos));
			if (lastDirection == Direction.LEFT) valeurDirection.put(Direction.LEFT, valeurDirection.get(Direction.LEFT) + 2 / levelMax);
		}
		if (niveau > repeat || lastDirection == Direction.NONE) {
			cpos = calculatePosition(Direction.NONE, pos, angle);
			valeurDirection.put(Direction.NONE, algoLocal(board, trail, cpos.getFirst(), resultat, cpos.getSecond(), posSnake, niveau + 1, pos));
			if (lastDirection == Direction.NONE) valeurDirection.put(Direction.NONE, valeurDirection.get(Direction.NONE) + 2 / levelMax);
		}
		if (niveau <= repeat) {
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
		if (niveau == repeat+1) {
			preventChoice(valeurDirection);
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
		if (none < right - 1.5 || none < left - 1.5) {
			prevent.add(Direction.NONE);
		}
		if (left < right - 1.5 || left < none - 1.5) {
			prevent.add(Direction.LEFT);

		}
		if (right < none - 1.5 || right < left - 1.5) {
			prevent.add(Direction.RIGHT);
		}
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
		if (realTime < timeMin) realTime = timeMin;
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

	/**
	 * @param board terrain de jeu
	 */
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
	 * @param board terrain de jeu
	 * @return vers ou sont les autres snakes (map de direction et nombre de snake)
	 */
	public HashMap < Direction, Double > getWhereSnakes(Board board) {
		HashMap < Direction, Double > snakeDirection = new HashMap < Direction, Double > ();
		snakeDirection.put(Direction.NONE, 0.0);
		snakeDirection.put(Direction.RIGHT, 0.0);
		snakeDirection.put(Direction.LEFT, 0.0);
		double dist;
		for (Snake snake: board.snakes) {
			  //obstacle
			
			
			dist = Math.sqrt(Math.pow(agentSnake.currentX - snake.currentX, 2) + Math.pow(agentSnake.currentY - snake.currentY, 2));
			if (snake != agentSnake && dist > 200 && snake.eliminatedBy==null) {
				double angle = modulo(Math.atan2(snake.currentY-agentSnake.currentY,snake.currentX-agentSnake.currentX),2*Math.PI);
				double angletmp=modulo(angle - currentAngle,2*Math.PI);
				if (angletmp>=3*Math.PI/2 || angletmp<=Math.PI/2) {
					if (angletmp<=Math.PI/2 - 0.175) {
						snakeDirection.put(Direction.RIGHT, snakeDirection.get(Direction.RIGHT) + 1);
					} else if (angletmp>=3*Math.PI/2 + 0.175) {
						snakeDirection.put(Direction.LEFT, snakeDirection.get(Direction.LEFT) + 1);
					} else {
						snakeDirection.put(Direction.NONE, snakeDirection.get(Direction.NONE) + 1);
					}
				} else
				{
					snakeDirection.put(Direction.LEFT, snakeDirection.get(Direction.LEFT) + 0.5);
					snakeDirection.put(Direction.RIGHT, snakeDirection.get(Direction.RIGHT) + 0.5);
				}
			}
		}
		return snakeDirection;
	}

	/**
	 * @param board le plateau de jeu
	 * @return une HashMap avec les 3 direction possible en cle et les note de chaque directions en valeur.
	 */
	private HashMap<Direction , Double> getWhereBonus(Board board) {
		checkInterruption(); // on doit tester l'interruption au début de chaque méthode
		// on compte le nombre de bonus
		int k = board.items.size();
		
		double note = 0.0;
		double note_tmp = 0.0;
		// création et initialisation à 0 de la map à retourner
		HashMap<Direction , Double> result = new HashMap < Direction, Double > ();
		result.put(Direction.LEFT, note);
		result.put(Direction.NONE, note);
		result.put(Direction.RIGHT, note);
		
		
		// si il n'y à pas de bonus les 3 chemins se valent donc on retourne la map tel quelle.
		if (k == 0) {
			return result;
		}
		
		// sinon on calcule pour chaques item son influence sur la note des directions.
		for (ItemInstance i: board.items) {
			checkInterruption(); // une boucle, donc un autre test d'interruption

			
			// on attribue une note selon le type de bonus
			switch (i.type) {
			case OTHERS_FAST:
				note = 3;
				break;
			case OTHERS_REVERSE:
				note = 3;
				break;
			case OTHERS_THICK:
				note = 7;
				break;
			case OTHERS_SLOW:
				note = 2;
				break;
			case USER_FAST:
				note = -5;
				break;
			case USER_FLY:
				note = 8;
				break;
			case USER_SLOW:
				note = -2;
				break;
			}
			// la note est modifié selon la distance entre les differents serpent et le bonus.
			if(Math.sqrt(Math.pow(agentSnake.currentX - i.x, 2) + Math.pow(agentSnake.currentY - i.y, 2))<200);
				note*=2;
			// on calcul l'angle entre la tete du de l'agent et le bonus
				double angle = modulo(Math.atan2(i.y-agentSnake.currentY,i.x-agentSnake.currentX),2*Math.PI);
				double angletmp=modulo(angle - currentAngle,2*Math.PI);
				if (angletmp>=3*Math.PI/2 || angletmp<=Math.PI/2) {
					if (angletmp<=Math.PI/2 - 0.175) {// le bonus est à droite, on met à jour la note de la direction droite.
						note_tmp=result.get(Direction.RIGHT)+note;
						result.put(Direction.RIGHT, note_tmp);
					} else if (angletmp>=3*Math.PI/2 + 0.175) {// le bonus est à gauche, on met à jour la note de la direction gauche.
						note_tmp=result.get(Direction.LEFT)+note;
						result.put(Direction.LEFT, note_tmp);
					} else { // le bonus est devant
						// on met à jour la note.
						note_tmp=result.get(Direction.NONE)+note;
						result.put(Direction.NONE, note_tmp);
					}
				} else // si le bonus est derrière , la note influe sur les cotes gauche et droit.
				{
					note_tmp=result.get(Direction.LEFT)+(note/2);
					result.put(Direction.LEFT, note_tmp);
					note_tmp=result.get(Direction.RIGHT)+(note/2);
					result.put(Direction.RIGHT, note_tmp);
				}
		}
		return result;
	}

	/**
	 * @param trail : les trainées des snake du board
	 * @return une HashMap avec en clé les direction et en valeur le nombre d'obstacle vers cette direction.
	 */
	public HashMap < Direction, Double > getWhereObstacles(Set<Position> trail) {
		HashMap < Direction, Double > obstacleDirection = new HashMap < Direction, Double > ();
		obstacleDirection.put(Direction.NONE, 0.0);
		obstacleDirection.put(Direction.RIGHT, 0.0);
		obstacleDirection.put(Direction.LEFT, 0.0);
		double dist;
		int nbObstacles = trail.size();
		for (Position obstacle:trail) {
			dist = Math.sqrt(Math.pow(agentSnake.currentX - obstacle.x, 2) + Math.pow(agentSnake.currentY - obstacle.y, 2));
			if (dist < 300) {
				double angle = modulo(Math.atan2(obstacle.y-agentSnake.currentY,obstacle.x-agentSnake.currentX),2*Math.PI);
				double angletmp=modulo(angle - currentAngle,2*Math.PI);
				if (angletmp>=3*Math.PI/2 || angletmp<=Math.PI/2) {
					if (angletmp<=Math.PI/2 - 0.175) {
						obstacleDirection.put(Direction.RIGHT, obstacleDirection.get(Direction.RIGHT) + 1);
					} else if (angletmp>=3*Math.PI/2 + 0.175) {
						obstacleDirection.put(Direction.LEFT, obstacleDirection.get(Direction.LEFT) + 1);
					} else {
						obstacleDirection.put(Direction.NONE, obstacleDirection.get(Direction.NONE) + 1);
					}
				} else
				{
					obstacleDirection.put(Direction.LEFT, obstacleDirection.get(Direction.LEFT) + 0.2);
					obstacleDirection.put(Direction.RIGHT, obstacleDirection.get(Direction.RIGHT) + 0.2);
				}
			}
		}
		
		obstacleDirection.put(Direction.NONE, obstacleDirection.get(Direction.NONE)/nbObstacles * 5);
		obstacleDirection.put(Direction.RIGHT, obstacleDirection.get(Direction.RIGHT)/nbObstacles * 5);
		obstacleDirection.put(Direction.LEFT, obstacleDirection.get(Direction.LEFT)/nbObstacles * 5);
		return obstacleDirection;
	}
	

	/**
	 * Détermine si on considère que la tête du serpent de l'agent
	 * se trouve dans un coin de l'aire de jeu.
	 * @param board  le terrain de jeu
	 *  
	 * @return
	 * 		{@code true} ssi l'agent est dans un coin et va en direction du coin.
	 */
	private boolean isInCorner(Board board) {
		checkInterruption(); // on doit tester l'interruption au début de chaque méthode
		double droite = currentAngle - Math.atan(120);
		double gauche = currentAngle + Math.atan(120);
		if(agentSnake.currentX < CORNER_THRESHOLD && agentSnake.currentY < CORNER_THRESHOLD)
		{
			double angle = Math.atan2(0-agentSnake.currentY, 0-agentSnake.currentX);
			if(estVisible(angle,droite,gauche))
			{
				return true;
			}
		}
		else if(board.width - agentSnake.currentX < CORNER_THRESHOLD && agentSnake.currentY < CORNER_THRESHOLD)
		{
			double angle = Math.atan2(board.width-agentSnake.currentY, 0-agentSnake.currentX);
			if(estVisible(angle,droite,gauche))
			{
				return true;
			}
		}
		else if(agentSnake.currentX < CORNER_THRESHOLD && board.height - agentSnake.currentY < CORNER_THRESHOLD)
		{
			double angle = Math.atan2(0-agentSnake.currentY, board.height-agentSnake.currentX);
			if(estVisible(angle,droite,gauche))
			{
				return true;
			}
		}
		else if(board.width - agentSnake.currentX < CORNER_THRESHOLD && board.height - agentSnake.currentY < CORNER_THRESHOLD)
		{
			double angle = Math.atan2(board.width-agentSnake.currentY, board.height-agentSnake.currentX);
			if(estVisible(angle,droite,gauche))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @param angle -> angle directionnel du snake
	 * @param droite -> vue droite du snake
	 * @param gauche -> vue gauche du snake
	 * @return -> return vrai si obstacle dans champs de vision
	 */
	private boolean estVisible(double angle, double droite, double gauche)
	{	checkInterruption();	// on doit tester l'interruption au début de chaque méthode
		boolean result = false;
		
		if(angle>=droite && angle<=gauche)
			result = true;

		// premier cas limite : si la borne supérieure dépasse 2PI
		// on teste si l'angle est inférieur à upperBound - 2pi.
		else if(gauche>2*Math.PI && angle<=gauche-2*Math.PI)
			result = true;
			
		// second cas limite : si la borne inférieure est négative 
		// on teste si l'angle est supérieur à lowerBound + 2PI
		else if(droite<0 && angle>=droite+2*Math.PI)
			result = true;
			
		return result;
	}
}
