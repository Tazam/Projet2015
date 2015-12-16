package fr.univavignon.courbes.graphics.groupe18;
import java.awt.Dimension;
import java.awt.Color; 
import java.awt.Graphics;
import java.util.Map;
import java.lang.Math;
import javax.swing.JPanel;
import fr.univavignon.courbes.common.Board;
import fr.univavignon.courbes.common.Position;

 
/**
 * La classe Draw sert a dessiner les snakes sur l'aire de jeu
 * @author uapv1504323 Antoine Letourneur
 * @author uapv1402334 Axel Clerici
 */
public class Draw extends JPanel { 
	/**
	 * Numéro de série (pour {@code Serializable})
	 */
	private static final long serialVersionUID = 1L;
	
	
	/**
	 * Aire de jeu
	 */
	private Board board;
	
    /**
     * Constructeur de la classe Draw pour construire le panel où la fonction paintComponent dessinera
     * @param X
     * 		Taille en X de la matrice 
     * @param Y
     * 		Taille en Y de la matrice 
     * @param board
     * 		L'aire de jeu
     */
    Draw(int X, int Y, Board board) {
        setPreferredSize(new Dimension(X, Y));
        this.board = board;
    }

    /**
     * Renvoie la couleur associée à l'id du joueur
     * @param id
     * 		L'id du joueur
     * @return Un objet Color de la couleur associée à ,l'id du joueur
     */
    public static Color getColor(int id) {
    	Color color = Color.black;
        switch (id)
        {
  			case 0:
  				color = Color.red; 
  		    break;
  			case 1:
  				color = Color.blue; 
  		    break;
  			case 2:
  				color = Color.green; 
  		    break;
  			case 3:
  				color = Color.white; 
  		    break;
  			case 4:
  				color = Color.cyan; 
  		    break;
  			case 5:
  				color = Color.magenta; 
  		    break;
  			case 6:
  				color = Color.pink; 
  		    break;
  			case 7:
  				color = Color.orange; 
  		    break;
  			case 8:
  				color = Color.lightGray; 
  		    break;
  		} 
        return color;
    }
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        for(int i = 0; i < board.snakes.length; i++) {
        	g.setColor(getColor(board.snakes[i].playerId));
        	int x = (int)Math.round(board.snakes[i].headRadius);
        	g.drawOval(board.snakes[i].currentX, board.snakes[i].currentY, x, x);
        	g.fillOval(board.snakes[i].currentX, board.snakes[i].currentY, x, x);
        	
        }
		for (Map.Entry<Position, Integer> entry : board.snakesMap.entrySet())
		{
			g.setColor(getColor(entry.getValue()));
			g.fillRect(entry.getKey().x, entry.getKey().y, 1, 1);
		}
        
    }          
}