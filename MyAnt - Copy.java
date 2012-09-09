import ants.*;
import java.util.*;

public class MyAnt implements Ant{

    
    private Direction lastDir = null;
    
    private int foundFoodX = 0;
    private int foundFoodY = 0;
    private boolean isCarryingFood = false;
    private int maxPatience = 0; 
    private int currPatience = 0;
    
    //If the ant has exceeded this amount of steps without accomplishing anything, change its behavior
    private int maxIdle = 50;  
    private int currentIdle = 0;
    private int behaviorType = 0;
        
    // Current position - the nest is at 0,0
    private int currX = 0;
    private int currY = 0;
    
    public MyAnt()
    {
    	// Create random patience value. This way each ant behaves differently.
    	Random randomGenerator = new Random();
	    maxPatience = randomGenerator.nextInt(15)+5;
	    
	    // Pick an initial direction to move in
	    int randomInt = randomGenerator.nextInt(4);
	    if(randomInt == 0 )
	    {
	    	lastDir = Direction.NORTH;
	    			    	
	    }
	    if(randomInt == 1 )
	    {
	    	lastDir = Direction.EAST;
	    		    	
	    }
	    if(randomInt == 2 )
	    {
	    	lastDir = Direction.SOUTH;
	    		    	
	    }
	    if(randomInt == 3 )
	    {
	    	lastDir = Direction.WEST;
	    		    	
	    }
	    
    }
    
	public Action getAction(Surroundings surroundings){
		
		if(currX == 0 && currY == 0 && isCarryingFood == true)
		{
			// We have returned to the nest with food, drop it off
			isCarryingFood = false;
			lastDir = null;
			currentIdle = 0;
					
			Random randomGenerator = new Random();
		    maxPatience = randomGenerator.nextInt(15)+5;
		    
		    if(foundFoodX==0 && foundFoodY ==0)
		    {
		    	//Set a new random direction to head off to
		    	// Pick an initial direction to move in
			    int randomInt = randomGenerator.nextInt(4);
			    if(randomInt == 0 )
			    {
			    	lastDir = Direction.NORTH;
			    			    	
			    }
			    if(randomInt == 1 )
			    {
			    	lastDir = Direction.EAST;
			    		    	
			    }
			    if(randomInt == 2 )
			    {
			    	lastDir = Direction.SOUTH;
			    		    	
			    }
			    if(randomInt == 3 )
			    {
			    	lastDir = Direction.WEST;
			    		    	
			    }
		    	
		    }
		    
			return Action.DROP_OFF;
			
		}
		
		//If we are at the nest and don't know where the food is, wait a bit for an ant with food to return and tell us
		//the location before wandering off on our own.
		if(currX == 0 && currY == 0 && isCarryingFood == false && surroundings.getCurrentTile().getAmountOfFood()>0 
				&& foundFoodX == 0 && foundFoodY == 0 && currentIdle < 10)
		{
			currentIdle++;
			return Action.HALT;
		}
		
		// Check if we are on a tile that has food
		if(surroundings.getCurrentTile().getAmountOfFood()>0 && isCarryingFood == false && !(currX == 0 && currY ==0))
		{
			lastDir = null;			
			isCarryingFood = true;
			currentIdle = 0;
			
			if(surroundings.getCurrentTile().getAmountOfFood() > 1)
			{
				
				//If there's food remaining, remember this food location
				foundFoodY = currY;
				foundFoodX = currX;
			}
			else
			{
				foundFoodY = 0;
				foundFoodX = 0;				
			}
			
			return Action.GATHER;
		}
		
		// Move to the spot that has food
		if(surroundings.getTile(Direction.NORTH).getAmountOfFood() > 0 && !isCarryingFood && !(currX==0 && currY==-1))
		{
			lastDir = Direction.NORTH;
			UpdateXY(lastDir);
			return Action.move(Direction.NORTH);			
		}
		
		if(surroundings.getTile(Direction.SOUTH).getAmountOfFood() > 0 && !isCarryingFood && !(currX==0 && currY==1))
		{
			lastDir = Direction.SOUTH;
			UpdateXY(lastDir);
			return Action.move(Direction.SOUTH);			
		}
		
		if(surroundings.getTile(Direction.EAST).getAmountOfFood() > 0 && !isCarryingFood && !(currX==-1 && currY==0))
		{
			lastDir = Direction.EAST;
			UpdateXY(lastDir);
			return Action.move(Direction.EAST);			
		}
		
		if(surroundings.getTile(Direction.WEST).getAmountOfFood() > 0 && !isCarryingFood && !(currX==1 && currY==0))
		{
			lastDir = Direction.WEST;
			UpdateXY(lastDir);
			return Action.move(Direction.WEST);			
		}
		
		// We're not carrying food and we haven't found any food				
		// See if we can keep going in the current direction
		if(isCarryingFood == false && foundFoodX == 0 && foundFoodY == 0)
		{
			return GoNext(surroundings);
			
		}
		   
					
		// We are trying to reach known food location, or return to nest with food	
		Direction correctDirX = null;
		Direction correctDirY = null;
		
		if(isCarryingFood == false)
		{
			// Find right directions for going towards food
			if(foundFoodX - currX > 0 )
				correctDirX = Direction.EAST;
			else if(foundFoodX - currX < 0)
				correctDirX = Direction.WEST;
			else correctDirX = null;
			
			if(foundFoodY - currY > 0 )
				correctDirY = Direction.NORTH;
			else if(foundFoodY - currY < 0)
				correctDirY = Direction.SOUTH;
			else correctDirY = null;
		} 
		else
		{
			// Find right directions for going back to nest
			if(currX < 0 )
				correctDirX = Direction.EAST;
			else if(currX > 0)
				correctDirX = Direction.WEST;
			else correctDirX = null;
			
			if(currY < 0 )
				correctDirY = Direction.NORTH;
			else if(currY > 0)
				correctDirY = Direction.SOUTH;
			else correctDirY = null;			
		
		}
		
		// Check to see if all the food from this tile has been consumed
		if(correctDirY == null && correctDirX == null)
		{			
				if(surroundings.getCurrentTile().getAmountOfFood() == 0)
				{
					foundFoodX = 0; foundFoodY = 0;
					// Pick a new initial direction to move in
					Random randomGenerator = new Random();
				    int randomInt = randomGenerator.nextInt(4);
				    if(randomInt == 0 )
				    {
				    	lastDir = Direction.NORTH;
				    			    	
				    }
				    if(randomInt == 1 )
				    {
				    	lastDir = Direction.EAST;
				    		    	
				    }
				    if(randomInt == 2 )
				    {
				    	lastDir = Direction.SOUTH;
				    		    	
				    }
				    if(randomInt == 3 )
				    {
				    	lastDir = Direction.WEST;
				    		    	
				    }
				}
								
		}
		
		// Keep moving in the right direction if we can
		if(isCarryingFood == true || foundFoodX!=0 || foundFoodY!=0)
		{
			
			if(correctDirY!= null)
			{
				if(surroundings.getTile(correctDirY).isTravelable() && 
					((lastDir==correctDirY) || (lastDir==correctDirX) || (correctDirY!=GetOpposite(lastDir) || lastDir == null)))
				{
					lastDir = correctDirY;
					UpdateXY(correctDirY);
					currentIdle++;
					return Action.move(correctDirY);
				}
			}
			else if(correctDirX!= null && surroundings.getTile(correctDirX).isTravelable()  &&
					(lastDir==correctDirX || (lastDir==correctDirY) || (correctDirX!=GetOpposite(lastDir) || lastDir == null)))
			{
				lastDir = correctDirX;
				UpdateXY(correctDirX);
				currentIdle++;
				return Action.move(correctDirX);
			}
			
			
			// We cannot go in any correct direction, so find another direction to move in
			//if(correctDirX!=null) 
			//	lastDir = correctDirX;
			//else if(correctDirY!=null) 
			//	lastDir = correctDirY;
			return GoNext(surroundings);
			
		}
				
		return Action.HALT;	
	
	}
	
	public byte[] send(){
		
		// Send food location to other ant
		byte[] target = new byte[2];
		target[0] = (byte)foundFoodX;
		target[1] = (byte)foundFoodY;
		return target;
	}
	
	public void receive(byte[] data){
		// If we haven't found any food, or if there's closer food, get food location from the other ant
		int newDistance = Math.abs((int)data[0]) + Math.abs((int)data[1]);
		int origDistance = Math.abs((int)foundFoodX) + Math.abs((int)foundFoodY);
		
		if(foundFoodX == 0 && foundFoodY == 0 || newDistance < origDistance)
		{
			foundFoodX = (int)data[0];
			foundFoodY = (int)data[1];
			
		}
	}
	
	// Helper function to update our current coordinate
	private void UpdateXY(Direction d)
	{
		lastDir = d;
		if(d == Direction.NORTH)
			currY++;
		if(d == Direction.SOUTH)
			currY--;
		if(d == Direction.EAST)
			currX++;
		if(d == Direction.WEST)
			currX--;
		
	}
	
	
	private Direction GetOpposite(Direction dir)
	{
		if(dir == Direction.NORTH)
			return Direction.SOUTH;
		if(dir == Direction.SOUTH)
			return Direction.NORTH;
		if(dir == Direction.EAST)
			return Direction.WEST;
		else return Direction.EAST;
	}
	
	// Choose the next direction to move in
	private Action GoNext(Surroundings surroundings)
	{
		// See if we can keep going in same direction
		if(lastDir!=null && surroundings.getTile(lastDir).isTravelable() && currPatience <= maxPatience)
		{
			currPatience++;
			UpdateXY(lastDir);
			return Action.move(lastDir);
			
		}
		// If we hit a wall or run out of patience, we need to change direction
		else
		{
			if(currPatience > maxPatience)
				currPatience = 0;
			
			currentIdle++;
			// Change ant's turning behavior if nothing has happened for too long
			if(currentIdle > maxIdle)
			{				
				maxPatience = 5;
				//behaviorType = (behaviorType==0) ? 1 : 0;
				if(behaviorType ==0)
					behaviorType = 1;
				else if(behaviorType ==1)
					behaviorType = 0;
				currentIdle = 0;
			}
						
			// only backtrack as last resort, make sure we don't get stuck in circles
			if(behaviorType == 0)
			{
				if(lastDir == Direction.NORTH)
			    {
					if(surroundings.getTile(Direction.WEST).isTravelable())
					{					
						UpdateXY(Direction.WEST);
						return Action.move(Direction.WEST);
					} else 
					if(surroundings.getTile(Direction.EAST).isTravelable())
					{					
						UpdateXY(Direction.EAST);
						return Action.move(Direction.EAST);
					}
					else
						if(surroundings.getTile(Direction.SOUTH).isTravelable())
					{					
						UpdateXY(Direction.SOUTH);
						return Action.move(Direction.SOUTH);
						
					}
					UpdateXY(Direction.NORTH);
					return Action.move(Direction.NORTH);
			    }
				
				else if(lastDir == Direction.SOUTH)
			    {
					if(surroundings.getTile(Direction.EAST).isTravelable())
					{					
						UpdateXY(Direction.EAST);
						return Action.move(Direction.EAST);
					} else 
					if(surroundings.getTile(Direction.WEST).isTravelable())
					{					
						UpdateXY(Direction.WEST);
						return Action.move(Direction.WEST);
					}
					else if(surroundings.getTile(Direction.NORTH).isTravelable())
					{					
						UpdateXY(Direction.NORTH);
						return Action.move(Direction.NORTH);
						
					}
					UpdateXY(Direction.SOUTH);
					return Action.move(Direction.SOUTH);
			    }
			    	
				else if(lastDir == Direction.EAST)
			    {
					if(surroundings.getTile(Direction.SOUTH).isTravelable())
					{					
						UpdateXY(Direction.SOUTH);
						return Action.move(Direction.SOUTH);
					} else 
					if(surroundings.getTile(Direction.NORTH).isTravelable())
					{					
						UpdateXY(Direction.NORTH);
						return Action.move(Direction.NORTH);
					}
					else if(surroundings.getTile(Direction.WEST).isTravelable())
					{					
						UpdateXY(Direction.WEST);
						return Action.move(Direction.WEST);
						
					}
					UpdateXY(Direction.EAST);
					return Action.move(Direction.EAST);
			    }
				
				else if (lastDir == Direction.WEST)
			    {
					if(surroundings.getTile(Direction.NORTH).isTravelable())
					{					
						UpdateXY(Direction.NORTH);
						return Action.move(Direction.NORTH);
					} else 
					if(surroundings.getTile(Direction.SOUTH).isTravelable())
					{					
						UpdateXY(Direction.SOUTH);
						return Action.move(Direction.SOUTH);
					}
					else if(surroundings.getTile(Direction.EAST).isTravelable())
					{					
						UpdateXY(Direction.EAST);
						return Action.move(Direction.EAST);
						
					}
					UpdateXY(Direction.WEST);
					return Action.move(Direction.WEST);
			    }
				else 
			    {
					if(surroundings.getTile(Direction.NORTH).isTravelable())
					{					
						UpdateXY(Direction.NORTH);
						return Action.move(Direction.NORTH);
					} else 
					if(surroundings.getTile(Direction.SOUTH).isTravelable())
					{					
						UpdateXY(Direction.SOUTH);
						return Action.move(Direction.SOUTH);
					}
					else if(surroundings.getTile(Direction.EAST).isTravelable())
					{					
						UpdateXY(Direction.EAST);
						return Action.move(Direction.EAST);
						
					}
					UpdateXY(Direction.WEST);
					return Action.move(Direction.WEST);
			    }
			}
			else
			{
				if(lastDir == Direction.NORTH)
			    {
					if(surroundings.getTile(Direction.EAST).isTravelable())
					{					
						UpdateXY(Direction.EAST);
						return Action.move(Direction.EAST);
					} else 
					if(surroundings.getTile(Direction.WEST).isTravelable())
					{					
						UpdateXY(Direction.WEST);
						return Action.move(Direction.WEST);
					}
					else
						if(surroundings.getTile(Direction.SOUTH).isTravelable())
					{					
						UpdateXY(Direction.SOUTH);
						return Action.move(Direction.SOUTH);
						
					}
					UpdateXY(Direction.NORTH);
					return Action.move(Direction.NORTH);
			    }
				
				else if(lastDir == Direction.SOUTH)
			    {
					if(surroundings.getTile(Direction.WEST).isTravelable())
					{					
						UpdateXY(Direction.WEST);
						return Action.move(Direction.WEST);
					} else 
					if(surroundings.getTile(Direction.EAST).isTravelable())
					{					
						UpdateXY(Direction.EAST);
						return Action.move(Direction.EAST);
					}
					else if(surroundings.getTile(Direction.NORTH).isTravelable())
					{					
						UpdateXY(Direction.NORTH);
						return Action.move(Direction.NORTH);
						
					}
					UpdateXY(Direction.SOUTH);
					return Action.move(Direction.SOUTH);
			    }
			    	
				else if(lastDir == Direction.EAST)
			    {
					if(surroundings.getTile(Direction.NORTH).isTravelable())
					{					
						UpdateXY(Direction.NORTH);
						return Action.move(Direction.NORTH);
					} else 
					if(surroundings.getTile(Direction.SOUTH).isTravelable())
					{					
						UpdateXY(Direction.SOUTH);
						return Action.move(Direction.SOUTH);
					}
					else if(surroundings.getTile(Direction.WEST).isTravelable())
					{					
						UpdateXY(Direction.WEST);
						return Action.move(Direction.WEST);
						
					}
					UpdateXY(Direction.EAST);
					return Action.move(Direction.EAST);
			    }
				
				else if (lastDir == Direction.WEST)
			    {
					if(surroundings.getTile(Direction.SOUTH).isTravelable())
					{					
						UpdateXY(Direction.SOUTH);
						return Action.move(Direction.SOUTH);
					} else 
					if(surroundings.getTile(Direction.NORTH).isTravelable())
					{					
						UpdateXY(Direction.NORTH);
						return Action.move(Direction.NORTH);
					}
					else if(surroundings.getTile(Direction.EAST).isTravelable())
					{					
						UpdateXY(Direction.EAST);
						return Action.move(Direction.EAST);
						
					}
					UpdateXY(Direction.WEST);
					return Action.move(Direction.WEST);
			    }
				else 
			    {
					if(surroundings.getTile(Direction.SOUTH).isTravelable())
					{					
						UpdateXY(Direction.SOUTH);
						return Action.move(Direction.SOUTH);
					} else 
					if(surroundings.getTile(Direction.NORTH).isTravelable())
					{					
						UpdateXY(Direction.NORTH);
						return Action.move(Direction.NORTH);
					}
					else if(surroundings.getTile(Direction.WEST).isTravelable())
					{					
						UpdateXY(Direction.WEST);
						return Action.move(Direction.WEST);
						
					}
					UpdateXY(Direction.EAST);
					return Action.move(Direction.EAST);
			    }
								
				
			}
						    
		   
		}
		
	}

	

}