package com.soulpillars;

import android.view.Window;
import android.view.WindowManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.KeyEvent;
import android.media.SoundPool;
import android.media.AudioManager;
import android.graphics.Typeface;
import java.util.*;

public class BlockGameEngine extends GameObject
{
	//-------------------------------------------------------------------------------------	
	public static final int MAX_PLAY_FIELD_LAYERS = 4;
	public final static int MAX_SHAPE_TYPES = 16;	 
	public static int LayersCap = 2;
	public static int ShapeTypesCap = 7;  		
	public static int ChallengeShapeTypesCap = 15;
	public static int TraditionalShapeTypesCap = 7;
	public static int TotalRandomFlag = 0;
			
	public static final int CELL_WIDTH_PIXELS = 20;
	public static final int CELL_HEIGHT_PIXELS = 20;
	
	public static final int STYLE_PRIMITIVE = 1;
	public static final int STYLE_PICTURE = 2;
	
	protected BlockShape[] BlockShapeList;
	protected static int CurrentGameType;
	protected static int BlockDoneFallingCount;
	
	protected Random BGERandom;
	protected int[] BagRandomNumberList;
	protected int BagRandomNumberListIndex;
	protected static int BagRandomNumberListLength;
	
	public static final int MAX_FALL_DELAY = 50;
	protected static int MainFallDelay = 50;
    
	public static int GameOverFlag;

	public final static int SHAPE_WIDTH = 5;
	public final static int SHAPE_HEIGHT = 5;   
    public static int FIELD_GRID_WIDTH = GameGlobals.FIELD_GRID_WIDTH;
	public static int FIELD_GRID_HEIGHT = GameGlobals.FIELD_GRID_HEIGHT;	
		
	protected QuickFallBlockShape[] QuickFallBlockShapeList = new QuickFallBlockShape[MAX_PLAY_FIELD_LAYERS];
	
	protected ParabolicProjectileCells BlowUpObject = new ParabolicProjectileCells();
	
	//-------------------------------------------------------------------------------------	
    int BagRandomNumber( int func, int UpperLimit )
    {
      int ReturnValue = 0;
      int i, x, y, z;
      
      if( func == 0 )
      {
        // initialization
        BagRandomNumberListIndex = 0;
        BagRandomNumberListLength = UpperLimit;        
        BagRandomNumberList = new int[UpperLimit];

        for( i = 0; i < UpperLimit; i++ )
             BagRandomNumberList[i] = i;
             
        for( i = 0; i < UpperLimit ; i++ )
        {
          x = BGERandom.nextInt(UpperLimit);
          y = BGERandom.nextInt(UpperLimit);
          z = BagRandomNumberList[x];
          BagRandomNumberList[x] = BagRandomNumberList[y];
          BagRandomNumberList[y] = z;
        }      
      }
      else
      if( func == 1 )
      {
        // usage        
        ReturnValue = BagRandomNumberList[BagRandomNumberListIndex];
        BagRandomNumberListIndex++;
        
        if( BagRandomNumberListIndex >= BagRandomNumberListLength )
        {
            BagRandomNumberListIndex = 0;
             
        for( i = 0; i < BagRandomNumberListLength; i++ )
        {
          x = BGERandom.nextInt(BagRandomNumberListLength);
          y = BGERandom.nextInt(BagRandomNumberListLength);
          z = BagRandomNumberList[x];
          BagRandomNumberList[x] = BagRandomNumberList[y];
          BagRandomNumberList[y] = z;
        }             
        }
      }
      
      return ReturnValue;
    }	
	//-------------------------------------------------------------------------------------
	class BlockShape extends GameObject
	{
	 public final static int SHAPE_WIDTH = 5;
	 public final static int SHAPE_HEIGHT = 5;	 

	 //*** remove SHAPE_T ... SHAPE_O ***
	 public final static int SHAPE_T = 1;
	 public final static int SHAPE_Z = 2;
	 public final static int SHAPE_S = 3;
	 public final static int SHAPE_J = 4;
     public final static int SHAPE_L = 5;
	 public final static int SHAPE_I = 6;
     public final static int SHAPE_O = 7;
		 
     protected int Id;		
     protected int ArrayIndex;
     
   	 protected char[][] BlockData;
	 
	 protected PlayField ItsPlayField;
	 protected boolean GhostFlag;
	 protected int FallDelay;
	 protected int FallDelayCounter;
	 protected int RotateDelay;
	 protected int RotateDelayCounter;
	 protected int QuickFallDelay;
	 protected int QuickFallDelayCounter;
	 protected int HorizontalDelay;
	 protected int HorizontalDelayCounter;
	 protected int InitialGridX;
	 protected int InitialGridY;
	 
     protected int[] ColorList;
     public int DrawStyle;
     
     protected Random Rand;
     
     protected int[] NextPieceList;
     protected int NextPieceListIndex;     
     protected BlockShape NextPieceObj;
     
     protected int EnabledFlag;
     protected BlockShape[] ItsArray;
     public int VisibleFlag = 1;
     
     protected BlockGameEngine ItsBlockGameEngine = null;
     
     public final static int REGULAR_PIECE_ALPHA_VALUE = 255;
     public final static int NEXT_PIECE_ALPHA_VALUE = 255;
     public int AlphaValue = REGULAR_PIECE_ALPHA_VALUE;
     
	//------------------------------------------------------------------------------------- 
     protected void DecreaseCounters()
     {  
       if( RotateDelayCounter > 0 )
    	   RotateDelayCounter--;
       if( QuickFallDelayCounter > 0 )
    	   QuickFallDelayCounter--;
       if( HorizontalDelayCounter > 0 )
    	   HorizontalDelayCounter--;
     }
 	//-------------------------------------------------------------------------------------      
	 public BlockShape()
	 {
	   super();
	   BlockData = new char[SHAPE_WIDTH][SHAPE_HEIGHT];	
	   ItsPlayField = null;
	   GhostFlag = false;
	   FallDelay = MainFallDelay;
	   FallDelayCounter = FallDelay;
   
	   RotateDelay = 4;
	   RotateDelayCounter = 4;
	   QuickFallDelay = 8;
	   QuickFallDelayCounter = 8;
	   HorizontalDelay = 4;
	   HorizontalDelayCounter = 4;	   

	   //** specific for android STB **
/*       
	   RotateDelay = 8;
	   RotateDelayCounter = 8;
	   QuickFallDelay = 8;
	   QuickFallDelayCounter = 8;
	   HorizontalDelay = 8; 
	   HorizontalDelayCounter = 8; 
*/ 
	   
	   DrawStyle = STYLE_PICTURE;	   
       Rand = new Random();
       
	  int i;
      ColorList = new int[MAX_SHAPE_TYPES + 1];
           
      for( i = 0; i < MAX_SHAPE_TYPES + 1; i++ )
        ColorList[i] = Color.rgb(i * 10 + 10, i * 10 + 10, i * 10 + 10);
     
      ColorList[0] = Color.BLACK;
      ColorList[1] = Color.RED;
      ColorList[2] = Color.BLUE;
      ColorList[3] = Color.GREEN;
      ColorList[4] = Color.CYAN;
      ColorList[5] = Color.MAGENTA;
      ColorList[6] = Color.LTGRAY;      
      ColorList[7] = Color.YELLOW; 
      
      NextPieceList = new int[MAX_SHAPE_TYPES];
      NextPieceListIndex = 0;
      NextPieceObj = null;
      ArrayIndex = 0;
      EnabledFlag = 1;
      ItsArray = null;
	 }
	//------------------------------------------------------------------------------------- 
	public void Init( int StartGridX, int StartGridY, PlayField pf, boolean bGhostFlag, int nFallDelay,
		BlockShape NextPieceObjAttach, int nArrayIndex, BlockShape[] pItsArray, BlockGameEngine pBlockGameEngine ) 
	{
     ArrayIndex = nArrayIndex;
	 GridX = StartGridX; GridY = StartGridY;
	 InitialGridX = StartGridX; InitialGridY = StartGridY;
	 ItsPlayField = pf;
	 GhostFlag = bGhostFlag;
	 FallDelay = nFallDelay;
	 NextPieceObj = NextPieceObjAttach;
	 ItsArray = pItsArray;
	 GenerateRandomShapeBagMethod();		 
	 ItsBlockGameEngine = pBlockGameEngine;
	}
	//-------------------------------------------------------------------------------------
	public void CopyNextBlockShapeList()
	{
	  int i;
	  
	  for( i = 0; i < LayersCap; i++ )
	   {
  	    TheNextBlockShape[i].CopyBlock( NextPieceObj ); 
  	    ThePlayBlockShape[i].CopyBlock( this );
  	    //ThePlayBlockShape[i].RefreshBlock();
  	    //TheNextBlockShape[i].RefreshBlock();
	   }
	}
	//-------------------------------------------------------------------------------------
    public void ChooseNextBlockLayer()
    {
      int Index, i;
      int NextIndex = -1;
      
      Index = Rand.nextInt(LayersCap);	
      
      for( i = 0; i < LayersCap; i++ )
      {
    	  if( TheNextBlockShape[i].EnabledFlag > 0 )
    		  NextIndex = i;
    		  
    	  ThePlayBlockShape[i].EnabledFlag = 0;
          TheNextBlockShape[i].EnabledFlag = 0;                  
      }
      
      if( NextIndex == -1 )
    	  NextIndex = 0;
      
      ThePlayBlockShape[NextIndex].EnabledFlag = 1;
      TheNextBlockShape[Index].EnabledFlag = 1;
    }
	//-------------------------------------------------------------------------------------    
    public void SetFallDelay( int nFallDelay )
    {
      FallDelay = nFallDelay;
    }
	//-------------------------------------------------------------------------------------	
	protected void Rebirth()
	{		
	  GridX = InitialGridX;
	  GridY = InitialGridY;	 
	  FallDelayCounter = 0;
	  QuickFallDelayCounter = QuickFallDelay;
	  
	  if( NextPieceObj != null )
	  {		
		CopyBlock(NextPieceObj);
		NextPieceObj.GenerateRandomShapeBagMethod();
		NextPieceObj.AlphaValue = NEXT_PIECE_ALPHA_VALUE;
/*			
		 if( CurrentGameType == GameGlobals.GAME_TYPE_ORIGINAL )

		if( CurrentGameType == GameGlobals.GAME_TYPE_ORIGINAL ||
			CurrentGameType == GameGlobals.GAME_TYPE_CHALLENGE )
*/			
		 {		  	 		
		   	 ChooseNextBlockLayer();
		   	 CopyNextBlockShapeList();
		 }		 
	  }
	  VisibleFlag = 1;
	}
	//-------------------------------------------------------------------------------------
	protected int SharePieceLayers()
	{
	  // the cells of the shape is shared evenly among different layers	

	  int x, y, i, Index, Index2;
	  char Cell;
	  int ActiveCellCount = 0;
	  int UselessShapeCount = 0;
	  
	  int MAX_CELLS = 10;
	  int[] FilledCellX = new int[MAX_CELLS];
	  int[] FilledCellY = new int[MAX_CELLS];
	  int FilledCellCount = 0;
	  
	  for( y = 0; y < SHAPE_HEIGHT; y++ )
	    for( x = 0; x < SHAPE_WIDTH; x++ )
	    {
/*	    	
	      Cell = ItsArray[0].BlockData[x][y];
	      
	      for( i = 0; i < LayersCap; i++ )
	        ItsArray[i].BlockData[x][y] = 0;
	    
	      if( Cell > 0 )
	      {
	        Index = BagRandomNumber( 1, LayersCap );
	        ItsArray[Index].Id = Cell;
	        ItsArray[Index].BlockData[x][y] = (char)(ItsArray[Index].ArrayIndex + 1);
	      }
*/
	      
	      Cell = ItsArray[0].BlockData[x][y];	

	      for( i = 1; i < LayersCap; i++ )
		    	ItsArray[i].BlockData[x][y] = 0;	      
	      
	      if( Cell > 0 )
	      {	    	  		      
		      for( i = 1; i < LayersCap; i++ )
			    	ItsArray[i].BlockData[x][y] = (char)(ItsArray[i].ArrayIndex + 1);

		      FilledCellX[FilledCellCount] = x;
		      FilledCellY[FilledCellCount] = y;		      
		      FilledCellCount++;
		      /*
	    	 Index = BagRandomNumber( 1, LayersCap );  
	    	 ItsArray[Index].Id = Cell;
	    	 ItsArray[Index].BlockData[x][y] = 0;
	    	 */
	      }
	    }		
	 
	  for( i = 0; i < LayersCap; i++ )
	  {
		Index = BGERandom.nextInt(LayersCap);
		Index2 = BGERandom.nextInt(FilledCellCount);
		ItsArray[i].BlockData[ FilledCellX[Index2] ][ FilledCellY[Index2] ] = 0;
	  }
	  
      for( i = 0; i < LayersCap; i++ )
      {
    	for( y = 0; y < SHAPE_HEIGHT; y++ )
    	  for( x = 0; x < SHAPE_WIDTH; x++ )
    		if( ItsArray[i].BlockData[x][y] > 0 )
    		   ActiveCellCount++;
    	
    	if( ActiveCellCount <= 0 )
    		UselessShapeCount++;
      }
      return UselessShapeCount;
	}
	//-------------------------------------------------------------------------------------	
	protected void GenerateRandomShapeBagMethod()
	{
	  int x, y, z, i;
	  int upper, lower, r;	  
	  int nShapeTypesCap = 0;
	  
	  TotalRandomFlag = GameGlobals.UseClassicRandomMethodFlag;

	  if( CurrentGameType == GameGlobals.GAME_TYPE_ORIGINAL )
		  nShapeTypesCap = ShapeTypesCap;
	  else
	  if( CurrentGameType == GameGlobals.GAME_TYPE_CHALLENGE )
		  nShapeTypesCap = ChallengeShapeTypesCap;		  
	  else
	  if( CurrentGameType == GameGlobals.GAME_TYPE_TRADITIONAL )
          nShapeTypesCap = TraditionalShapeTypesCap;
	  
	  if( NextPieceListIndex >= nShapeTypesCap )
		  NextPieceListIndex = 0;
	  
	  if( NextPieceListIndex == 0 )
	  {
		 for( i = 0; i < nShapeTypesCap; i++ )
		  NextPieceList[i] = i + 1;
		 
		 for( i = 0; i < nShapeTypesCap; i++ )
		 {
			x = Rand.nextInt(nShapeTypesCap);	
			y = Rand.nextInt(nShapeTypesCap);
			z = NextPieceList[x];
			NextPieceList[x] = NextPieceList[y];
			NextPieceList[y] = z;			
		 }
	  }
	  
	  if( CurrentGameType == GameGlobals.GAME_TYPE_CHALLENGE )
	  {
/*		  
		if( ArrayIndex != 0 )	  			
	      GenerateShape( ItsArray[0].Id );
		else
  		 {
            if( TotalRandomFlag > 0 )
            {
			  upper = 17;	lower = 1;
			  r =  Rand.nextInt(upper - lower + 1) + lower;
			  GenerateShape(r);				
            }
            else
			  GenerateShape( NextPieceList[NextPieceListIndex] );
		 }
		
		if( ArrayIndex == LayersCap - 1)
			SharePieceLayers();
*/		
			 if( TotalRandomFlag > 0 )
	         {
				  upper = nShapeTypesCap;	lower = 1;
				  r =  Rand.nextInt(upper - lower + 1) + lower;
				  GenerateShape(r);				
	         }			 
			 else
			 GenerateShape( NextPieceList[NextPieceListIndex]);		  
		  
	  }	  
	  else		  
	  if( CurrentGameType == GameGlobals.GAME_TYPE_TRADITIONAL )
	  {

        if( TotalRandomFlag > 0 )
        {
 		  upper = nShapeTypesCap + 20;	lower = 21;
		  r =  Rand.nextInt(upper - lower + 1) + lower;
		  GenerateShape(r);
        }
        else
		  GenerateShape( NextPieceList[NextPieceListIndex]  + 20 );
	  }		
	  else
	  {
  	    
		 if( TotalRandomFlag > 0 )
         {
			  upper = nShapeTypesCap;	lower = 1;
			  r =  Rand.nextInt(upper - lower + 1) + lower;
			  GenerateShape(r);				
         }			 
		 else
		 GenerateShape( NextPieceList[NextPieceListIndex]);
	  }
	    NextPieceListIndex++;

	}
	//-------------------------------------------------------------------------------------	
	protected void GenerateShape( int nId )
	{		
	  int x, y, i, k;
/*	  
	  if( !( 1 <= nId && nId <= MAX_SHAPE_TYPES ))
		  return;
*/	  
	  String[][] slist = new String[MAX_SHAPE_TYPES + 1][SHAPE_HEIGHT];

	  slist[0][ 0] = ".....";
      slist[0][ 1] = ".....";
      slist[0][ 2] = ".....";
      slist[0][ 3] = ".....";
      slist[0][ 4] = ".....";
	  
	  slist[1][ 0] = ".....";   //*** standard pillar shape ***
      slist[1][ 1] = "..#..";
      slist[1][ 2] = "..#..";
      slist[1][ 3] = "..#..";
      slist[1][ 4] = ".....";

      slist[2][ 0] = ".....";
      slist[2][ 1] = "..#..";
      slist[2][ 2] = "..##.";
      slist[2][ 3] = "..#..";
      slist[2][ 4] = ".....";

      slist[3][ 0] = ".....";
      slist[3][ 1] = "..#..";
      slist[3][ 2] = ".##..";
      slist[3][ 3] = "..#..";
      slist[3][ 4] = ".....";

      slist[4][ 0] = ".....";
      slist[4][ 1] = ".##..";
      slist[4][ 2] = "..##.";
      slist[4][ 3] = ".....";
      slist[4][ 4] = ".....";

      slist[5][ 0] = ".....";
      slist[5][ 1] = "..##.";
      slist[5][ 2] = ".##..";
      slist[5][ 3] = ".....";
      slist[5][ 4] = ".....";

      slist[6][ 0] = ".....";
      slist[6][ 1] = "..#..";
      slist[6][ 2] = "..#..";
      slist[6][ 3] = "..#..";
      slist[6][ 4] = ".....";

      slist[7][ 0] = ".....";
      slist[7][ 1] = "..#..";
      slist[7][ 2] = "..#..";
      slist[7][ 3] = "..#..";
      slist[7][ 4] = ".....";
      
	  slist[8][ 0] = ".....";
      slist[8][ 1] = ".#.#.";
      slist[8][ 2] = "..#..";
      slist[8][ 3] = ".....";
      slist[8][ 4] = ".....";

	  slist[9][ 0] = ".....";
      slist[9][ 1] = ".....";
      slist[9][ 2] = ".###.";
      slist[9][ 3] = "..#..";
      slist[9][ 4] = ".....";
      
	  slist[10][ 0] = ".....";
      slist[10][ 1] = "..#..";
      slist[10][ 2] = "..##.";
      slist[10][ 3] = ".#...";
      slist[10][ 4] = ".....";
      
	  slist[11][ 0] = ".....";
      slist[11][ 1] = ".....";
      slist[11][ 2] = ".###.";
      slist[11][ 3] = ".....";
      slist[11][ 4] = ".....";      

	  slist[12][ 0] = ".....";
      slist[12][ 1] = ".....";
      slist[12][ 2] = ".#.#.";
      slist[12][ 3] = ".....";
      slist[12][ 4] = ".....";

	  slist[13][ 0] = ".....";
      slist[13][ 1] = "...#.";
      slist[13][ 2] = "..#..";
      slist[13][ 3] = ".....";
      slist[13][ 4] = ".....";
      
	  slist[14][ 0] = ".....";
      slist[14][ 1] = "..#..";
      slist[14][ 2] = "..##.";
      slist[14][ 3] = ".....";
      slist[14][ 4] = ".....";
      
	  slist[15][ 0] = ".....";
      slist[15][ 1] = ".....";
      slist[15][ 2] = ".##..";
      slist[15][ 3] = ".....";
      slist[15][ 4] = ".....";      
      

      char ch;
      //Id = nId;
      
      
   if( GameGlobals.CurrentGameType == GameGlobals.GAME_MODE_C )
	  Id = BGERandom.nextInt(7) + 1;
   else         
      Id = 1;   //*** choose the standard pillar shape ***   
      
      int Cell;
/*
      if( LayersCap > 1 )
          Cell = ArrayIndex + 1;
      else
      {
          Cell = Id;
          Cell = Cell % 7 + 1;
      }
*/      
            
      for( y = 0; y < SHAPE_HEIGHT; y++ )
       for( x = 0; x < SHAPE_WIDTH; x++ )
       {
    	 ch = slist[Id][y].charAt(x);
    	    	 
    	 if( ch == '#')
    	 {		 
    		 Cell = Rand.nextInt(GameGlobals.CellTypeCount) + 1; 
    		 BlockData[x][y] = (char)Cell;
    	 }
    	 else
    	 if( ch == '.')
    		 BlockData[x][y] = 0; 
       }      
	}
	//------------------------------------------------------------------------------------- 
	protected void RefreshBlock()
	{
	  int Cell, x, y;
	  	  
	      if( LayersCap > 1 )
	          Cell = ArrayIndex + 1;
	      else
	          Cell = Id;
	      
	      for( y = 0; y < SHAPE_HEIGHT; y++ )
	       for( x = 0; x < SHAPE_WIDTH; x++ )	
	    	 if( BlockData[x][y] != 0)
	    		 BlockData[x][y] = (char)Cell;
	          		
	}
	//------------------------------------------------------------------------------------- 	
	protected void CopyBlock( BlockShape src )
	{
       Id = src.Id;
   	   int x, y;
	  
       for( y = 0; y < SHAPE_HEIGHT; y++ )
          for( x = 0; x < SHAPE_WIDTH; x++ )
              BlockData[x][y] = src.BlockData[x][y];  
	}
	//------------------------------------------------------------------------------------- 	
	public void CopyBlockShapeData( BlockShape dest, BlockShape src )
	{		
	  int x, y;
	  
	      for( y = 0; y < SHAPE_HEIGHT; y++ )
	          for( x = 0; x < SHAPE_WIDTH; x++ )    	  	          
	             dest.BlockData[x][y] = src.BlockData[x][y];  
	      
	      dest.X = src.X;
	      dest.Y = src.Y;
	      dest.GridX = src.GridX;
	      dest.GridY = src.GridY;	      
	      dest.Id = src.Id;
	      dest.ItsPlayField = src.ItsPlayField;
	      dest.GhostFlag = src.GhostFlag;
	      dest.FallDelay = src.FallDelay;
	      dest.FallDelayCounter = src.FallDelayCounter;
	      dest.ColorList = src.ColorList;	
	}
	//------------------------------------------------------------------------------------- 	
	public void CopyBlockShapeDataDeep( BlockShape dest, BlockShape src )
	{	
		CopyBlockShapeData( dest, src );
		
		dest.EnabledFlag = src.EnabledFlag;
	    dest.ArrayIndex = src.ArrayIndex;
	    dest.NextPieceObj = src.NextPieceObj;
	    
	}
	//------------------------------------------------------------------------------------- 
	protected boolean CheckCollidePlayField()
	{
      boolean returnflag = false;
      char Cell = 0;
      int x, y, x2, y2, i;
      
      if( ItsPlayField == null )
    	  return returnflag;
      
      for( y = 0; y < SHAPE_HEIGHT; y++ )
       for( x = 0; x < SHAPE_WIDTH; x++ )
       {
    	 x2 = this.GridX + x;
    	 y2 = this.GridY + y;
    	 
    	 Cell = BlockData[x][y];
    	 
    	 if( Cell > 0 && 0 <= x2 && x2 < ItsPlayField.FIELD_GRID_WIDTH &&
    		 0 <= y2 && y2 < ItsPlayField.FIELD_GRID_HEIGHT )
    	   if( ItsPlayField.BlockData[x2][y2] > 0 )
    	   {
    		  returnflag = true;
    		  break;
    	   }
       }
   
      return returnflag;
	}
	//------------------------------------------------------------------------------------- 	
	public void RotateRight() 
	{
		  if( GhostFlag )
			  return;		
		  
		  if( RotateDelayCounter > 0 )
		      return;
		  
		  RotateDelayCounter = RotateDelay;
		      
		  BlockShape bs2 = new BlockShape();
		  int x, y, x2, y2;
		  
	          CopyBlockShapeData( bs2, this );
	      
          
          //*** shift down ***
          
          for( x = 0; x < SHAPE_WIDTH; x++ )
          {
            BlockData[x][1] = bs2.BlockData[x][3];
            BlockData[x][2] = bs2.BlockData[x][1];
            BlockData[x][3] = bs2.BlockData[x][2];
          }

	      if( CheckCollidePlayField() )
	      {
	    	 PlaySound(0);
	      	 CopyBlockShapeData( this, bs2 );   
	      }
	}
	//------------------------------------------------------------------------------------- 
	public void RotateLeft() 
	{
		  if( GhostFlag )
			  return;		
		  
		  if( RotateDelayCounter > 0 )
		      return;
		  
		  RotateDelayCounter = RotateDelay;
		      
		  BlockShape bs2 = new BlockShape();
		  int x, y, x2, y2;
		  
	          CopyBlockShapeData( bs2, this );
	      
          
          //*** shift up ***
          
          for( x = 0; x < SHAPE_WIDTH; x++ )
          {
            BlockData[x][1] = bs2.BlockData[x][2];
            BlockData[x][2] = bs2.BlockData[x][3];
            BlockData[x][3] = bs2.BlockData[x][1];
          }

	      if( CheckCollidePlayField() )
	      {
	    	 PlaySound(0);
	      	 CopyBlockShapeData( this, bs2 );   
	      }
	} 
	//------------------------------------------------------------------------------------- 
	public void MoveLeft() 
	{						  
	  if( GhostFlag )
		  return;
	  
	  if( HorizontalDelayCounter > 0 )
	      return;
	  
	  HorizontalDelayCounter = HorizontalDelay;
	  
		  BlockShape bs2 = new BlockShape();
 		  CopyBlockShapeData( bs2, this );	
	      this.GridX--;
	      if( CheckCollidePlayField() )
	      {
	    	 PlaySound(0);
	      	 CopyBlockShapeData( this, bs2 );   
	      }	
	} 
	//------------------------------------------------------------------------------------- 
	public void MoveRight() 
	{		  
		  if( GhostFlag )
			  return;
		  
		  if( HorizontalDelayCounter > 0 )
		      return;
		 
		  HorizontalDelayCounter = HorizontalDelay;
		  
		  BlockShape bs2 = new BlockShape();
 		  CopyBlockShapeData( bs2, this );	
	      this.GridX++;
	      if( CheckCollidePlayField() )
	      {
	    	 PlaySound(0);
	      	 CopyBlockShapeData( this, bs2 );   
	      }	
	}
	//------------------------------------------------------------------------------------- 
	protected boolean Fall()
	{
	  boolean ReachBottomFlag = false;	  
	  
	  if( FallDelayCounter > 0 )
	  {
		  FallDelayCounter--;
	      return ReachBottomFlag;
	  }
	  FallDelayCounter = FallDelay;
	  
		 BlockShape bs2 = new BlockShape();
 		 CopyBlockShapeData( bs2, this );	
 		  
		 this.GridY++;

	      if( CheckCollidePlayField() )
	      {	    		    	  
		    CopyBlockShapeData( this, bs2 );
		    
		    ReachBottomFlag = true;
		    
		    if( !GhostFlag )
		      PasteBlockPlayField(); 
		    PlaySound(1);
	      }		   
	  
	  return ReachBottomFlag;
	}
	//-------------------------------------------------------------------------------------
	protected void GroupRebirth()
	{
		int i;
		for( i = 0; i < LayersCap; i++ )
		{
		  ThePlayBlockShape[i].Rebirth();
		  ThePlayBlockShape[i].EnabledFlag = 1;
 		  ThePlayBlockShape[i].VisibleFlag = 1;
		}
		
		BlockDoneFallingCount = 0;	
	}
	//-------------------------------------------------------------------------------------
    public void MakeNewPiece()
    {
/*    	
     if( CurrentGameType == GameGlobals.GAME_TYPE_TRADITIONAL )
     {
  	  BlockDoneFallingCount++;
  	  EnabledFlag = 0;
  					
  	  if( BlockDoneFallingCount >= LayersCap  )
  		  GroupRebirth();
      }
      else			 
*/    	  
        Rebirth();
    }
	//-------------------------------------------------------------------------------------
	public void QuickFall() 
	{		
		  if( QuickFallDelayCounter > 0 )			
              return;
		  if( ItsPlayField.LineClearAnimationCounter < 255 )
			  return;
		  
		  QuickFallDelayCounter = QuickFallDelay;
		  PlaySound(1);
 		  
		  int y, grid_y_top = this.GridY;
 		  
 		  for( y = 0; y < BlockGameEngine.FIELD_GRID_HEIGHT; y++ )
 		  { 			  
 			  this.GridY++;
 			  
 		      if( CheckCollidePlayField() )
 		      { 		    	 		    	 
 			    this.GridY--;
 			    
 			    ItsBlockGameEngine.CreateFallWoosh( ArrayIndex, this,  grid_y_top, GridY );
 			     	
    		    ItsPlayField.StartShake();
 			    
 			    if( !GhostFlag )
 			    { 	
 			      PasteBlockPlayField(); 	
  			      //ItsPlayField.LinesYClearArray = ItsPlayField.CheckFormLinesB();
                 ItsPlayField.CellClearCount = ItsPlayField.CheckFormPillars( ItsPlayField.CellClearPlayField );
                 GameGlobals.Add2CurrentScorePillars(ItsPlayField.CellClearCount);

                  //if( ItsPlayField.LinesYClearArray.length >= 1 )
                  if( ItsPlayField.CellClearCount >= 1 )
                  {
                      ItsPlayField.LineClearAnimationCounter = 0;
                      ItsPlayField.ClearLinesFlag = 1;
                      VisibleFlag = 0;
                  }
                  else
                    MakeNewPiece();
 			    }
   			    break;
 		      } 			  	 
 		  }
		  
	}
	//------------------------------------------------------------------------------------- 
	protected void PasteBlockPlayField() 
	{		
	  int x, y, x2, y2;
	  
	  for( y = 0; y < SHAPE_HEIGHT; y++ )
	   for( x = 0; x < SHAPE_WIDTH; x++ )
	   {
		 x2 = this.GridX + x;
		 y2 = this.GridY + y;
		 
		 if( this.BlockData[x][y] > 0 && 0 <= x2 && x2 < ItsPlayField.FIELD_GRID_WIDTH &&
			 0 <= y2 && y2 < ItsPlayField.FIELD_GRID_HEIGHT )
			 ItsPlayField.BlockData[x2][y2] = this.BlockData[x][y];		
	   }
	  
	  if( GameGlobals.CurrentGameType == GameGlobals.GAME_MODE_C )
        ItsPlayField.ShiftBlocksDownPillars( ItsPlayField.CellClearPlayField, ItsPlayField.LEFT_WALL_X, 
       	  ItsPlayField.RIGHT_WALL_X, ItsPlayField.FLOOR_Y1 );
    	  
	  ItsPlayField.CopyPit2WidePlayField();
	}
	//-------------------------------------------------------------------------------------	
	public void Do() 
	{
	 if( !GhostFlag )
	 {	 
	  if( GridY == InitialGridY )
      {
		if( CheckCollidePlayField())
		{
			GameOverFlag = 1;
			return;
		}
      }

      if( ItsPlayField.LineClearAnimationCounter < 255 )
          ItsPlayField.LineClearAnimationCounter += PlayField.LineClearAnimationCounterIncrement;
      else
      if( ItsPlayField.ClearLinesFlag == 1 )
      {
          //ItsPlayField.ShiftBlocksDown( ItsPlayField.LinesYClearArray );
    	  ItsPlayField.ShiftBlocksDownPillars( ItsPlayField.CellClearPlayField, ItsPlayField.LEFT_WALL_X, 
    			  ItsPlayField.RIGHT_WALL_X, ItsPlayField.FLOOR_Y1 );
    	  
    	 //*** check for combos ***
         ItsPlayField.CellClearCount = ItsPlayField.CheckFormPillars( ItsPlayField.CellClearPlayField );
         GameGlobals.Add2CurrentScorePillars(ItsPlayField.CellClearCount);
        
         if( ItsPlayField.CellClearCount >= 1 )
          {
             ItsPlayField.LineClearAnimationCounter = 0;
             ItsPlayField.ClearLinesFlag = 1;
             VisibleFlag = 0;
          }    	  
         else
         {
          ItsPlayField.ClearLinesFlag = 0;
          ItsPlayField.LinesYClearArray = null;      
          ItsPlayField.CellClearCount = 0;
          MakeNewPiece();
         }
      }	  
      else 
   	  if( Fall() )
   	  {
   		PasteBlockPlayField();  
  		//ItsPlayField.LinesYClearArray = ItsPlayField.CheckFormLinesB();        
        ItsPlayField.CellClearCount = ItsPlayField.CheckFormPillars( ItsPlayField.CellClearPlayField );
        GameGlobals.Add2CurrentScorePillars(ItsPlayField.CellClearCount);
        
         //if( ItsPlayField.LinesYClearArray.length >= 1 )
         if( ItsPlayField.CellClearCount >= 1 )
          {
             ItsPlayField.LineClearAnimationCounter = 0;
             ItsPlayField.ClearLinesFlag = 1;
             VisibleFlag = 0;
          }
         else
           MakeNewPiece();
   	  }
 	  DecreaseCounters();
	 }
	}
	//-------------------------------------------------------------------------------------	
	public void Draw() 
	{		
	  int x, y; 
	  float fX, fY;
      char Cell;
      
      if( VisibleFlag == 0 )
    	  return;
      
          float WIDTH = CELL_WIDTH_PIXELS/2;
          float HEIGHT = CELL_HEIGHT_PIXELS/2;

          float XShift = 0, YShift = 0;
       
          if( ArrayIndex == 0 )
          {
            XShift = 0; YShift = 0; 
          }
          else
          if( ArrayIndex == 1 )
          {
            XShift = 1; YShift = 0; 
          }
          else
          if( ArrayIndex == 2 )
          {
            XShift = 0; YShift = 1; 
          }
          else
          if( ArrayIndex == 3 )
          {
            XShift = 1; YShift = 1; 
          }	  
          XShift *= WIDTH;
          YShift *= HEIGHT;
	 
	 if( DrawStyle == STYLE_PRIMITIVE )
	 {		 
	  int c_color; 

      for( y = 0; y < SHAPE_HEIGHT; y++ )
		for( x = 0; x < SHAPE_WIDTH; x++ )
		   if( BlockData[x][y] > 0  )
		   {	 
			   fX = (GridX + x) * CELL_WIDTH_PIXELS; 
			   fY = (GridY + y) * CELL_HEIGHT_PIXELS;

			   c_color = ColorList[BlockData[x][y]];
			       
			 if( ArrayIndex < LayersCap )  
			   GE.DrawBox( fX, fY, fX+CELL_WIDTH_PIXELS, fY+CELL_HEIGHT_PIXELS, GE.LAYER_2, c_color, 
					    GameEngine.G_STYLE_STROKE );
			   
			   GE.DrawBox( fX+XShift, fY+YShift, fX+XShift+WIDTH, fY+YShift+HEIGHT, 
					     GE.LAYER_3, c_color,GameEngine.G_STYLE_FILL );		

/*			   
			   GE.DrawBox( fX, fY, fX+CELL_WIDTH_PIXELS, fY+CELL_HEIGHT_PIXELS, GE.LAYER_3, ColorList[BlockData[x][y]], 
					    GameEngine.G_STYLE_FILL );
*/					    
		   }
	 }
	 else
     if( DrawStyle == STYLE_PICTURE )
     {
    	 
         for( y = 0; y < SHAPE_HEIGHT; y++ )
     		for( x = 0; x < SHAPE_WIDTH; x++ )
     		   if( BlockData[x][y] > 0  )
     		   {     			   
	     		   Cell = BlockData[x][y];
	     		   
	     		   if( Cell > 0  )
	     		   {
	    			   fX = (GridX + x) * CELL_WIDTH_PIXELS; 
	    			   fY = (GridY + y) * CELL_HEIGHT_PIXELS;
	    			   
	    			   BlockCell.DrawCellAlpha( (int)fX, (int)fY, Cell - 1, AlphaValue );	     				
	     		   }     			   
     		   }
     }
	 
	}
	//-------------------------------------------------------------------------------------		
	}
	//-------------------------------------------------------------------------------------    
	class PlayerControls extends GameObject
	{
	   BlockShape[] BlockShapeList;
	   int BlockShapeListCount;
       Button[] ButtonList;
       MultiStateButton MultiStateButton_1;
       
       protected static final int BUTTON_ROTATE_LEFT = 0;
       protected static final int BUTTON_ROTATE_RIGHT = 1;
       protected static final int BUTTON_MOVE_LEFT = 2;
       protected static final int BUTTON_MOVE_RIGHT = 3;
       protected static final int BUTTON_HARD_DROP = 4;
       protected static final int MAX_BUTTONS = 5;
       
       public static final int GROUP_ID_NONE = 0;
       public static final int UNIQUE_ID_BUTTON_ROTATE_LEFT = 200;
       public static final int UNIQUE_ID_BUTTON_ROTATE_RIGHT = 201;
       public static final int UNIQUE_ID_BUTTON_LEFT = 202;
       public static final int UNIQUE_ID_BUTTON_RIGHT = 203;
       public static final int UNIQUE_ID_BUTTON_DROP = 204;
       public static final int UNIQUE_ID_BUTTON_SWITCH_LAYER = 205;
       
       public int InputDelayCounter, InputDelayCounterMax;       
       public final static int BUTTON_Y_SHIFT = -5;
       
       public final static int ButtonAlphaValue = 255;
       
	   //-------------------------------------------------------------------------------------
	   public PlayerControls()
	   {
		 super();
		 MouseEventNotifyFlag = true;
		 KeyEventNotifyFlag = true;
		 BlockShapeListCount = 0;
		 BlockShapeList = new BlockShape[MAX_PLAY_FIELD_LAYERS];
		 ButtonList = new Button[MAX_BUTTONS];
		 		 
		 

		 /*	  
public Caption( int nX, int nY, int nGroupId, int nUniqueId, int nStyle, int nTextLayer, int nPictureLayer )
public void Init_1( String str, int nFontSize, int nColor, int nTextRelativeX, int nTextRelativeY, boolean bMultiLineFlag,
   int nDistanceBetweenLines )

      Caption1 = new Caption( 63, 70, gg.GROUP_ID_MENU_E, gg.UNIQUE_ID_CAPTION_HIGH_SCORES_LIST, Caption.STYLE_TEXT, -1, -1 );
      Caption1.Init_1( "High Scores", 20, gg.MainFontColor, 5, 10, true, 30 ); 
	  AddControl(Caption1);		
	  
       ButtonList[BUTTON_ROTATE_LEFT] = new Button("RL", 0,0,0,0);
       ButtonList[BUTTON_ROTATE_LEFT].Create_WidthxHeight( 10, 420 + BUTTON_Y_SHIFT, 96, 48, GROUP_ID_NONE, UNIQUE_ID_BUTTON_ROTATE_LEFT, GE.LAYER_1, GE.LAYER_2 , 
       GameControl.IMAGE_TILES_1x1, 14, 19, 255 );	    
 	        	  
*/	      

		 
       ButtonList[BUTTON_ROTATE_LEFT] = new Button("RL", 0,0,0,0);
       ButtonList[BUTTON_ROTATE_LEFT].Create_WidthxHeight( 10, 420 + BUTTON_Y_SHIFT, 48, 48, GROUP_ID_NONE, 
        UNIQUE_ID_BUTTON_ROTATE_LEFT, GE.LAYER_4, GE.LAYER_4, GameControl.IMAGE_TILES_1x1, 14, 19, ButtonAlphaValue );	

       ButtonList[BUTTON_ROTATE_RIGHT] = new Button("RR", 0,0,0,0);
       ButtonList[BUTTON_ROTATE_RIGHT].Create_WidthxHeight( 70, 420 + BUTTON_Y_SHIFT, 48, 48, GROUP_ID_NONE, 
        UNIQUE_ID_BUTTON_ROTATE_RIGHT, GE.LAYER_4, GE.LAYER_4, GameControl.IMAGE_TILES_1x1, 15, 19, ButtonAlphaValue );
       
       ButtonList[BUTTON_MOVE_LEFT] = new Button("ML", 0,0,0,0);
       ButtonList[BUTTON_MOVE_LEFT].Create_WidthxHeight( 130, 420 + BUTTON_Y_SHIFT, 48, 48, GROUP_ID_NONE, 
        UNIQUE_ID_BUTTON_LEFT, GE.LAYER_4, GE.LAYER_4, GameControl.IMAGE_TILES_1x1, 12, 19, ButtonAlphaValue );
       
       ButtonList[BUTTON_MOVE_RIGHT] = new Button("MR", 0,0,0,0);
       ButtonList[BUTTON_MOVE_RIGHT].Create_WidthxHeight( 190, 420 + BUTTON_Y_SHIFT, 48, 48, GROUP_ID_NONE, 
        UNIQUE_ID_BUTTON_RIGHT, GE.LAYER_4, GE.LAYER_4, GameControl.IMAGE_TILES_1x1, 13, 19, ButtonAlphaValue );

       ButtonList[BUTTON_HARD_DROP] = new Button("HD", 0,0,0,0);
       ButtonList[BUTTON_HARD_DROP].Create_WidthxHeight( 250, 420 + BUTTON_Y_SHIFT, 48, 48, GROUP_ID_NONE, 
        UNIQUE_ID_BUTTON_DROP, GE.LAYER_4, GE.LAYER_4, GameControl.IMAGE_TILES_1x1, 10, 19, ButtonAlphaValue );
       		 
		 int i;
		 for( i = 0; i < MAX_BUTTONS; i++ )
		 {
		   ButtonList[i].MouseEventNotifyFlag = true;
		   ButtonList[i].InputDelayMax = 0;
		   ButtonList[i].InputDelayCounter = 0;	   
		 }

		 
         MultiStateButton_1 = new MultiStateButton( 264, 364, 48, 48, GROUP_ID_NONE, UNIQUE_ID_BUTTON_SWITCH_LAYER, 
        	GE.LAYER_1, GE.LAYER_2, 4, GameControl.IMAGE_TILES_1x1, 0, 255 );
		 MultiStateButton_1.MouseEventNotifyFlag = true;
		 MultiStateButton_1.SetMaxStates( LayersCap );


		 InputDelayCounterMax = 5; 
		 InputDelayCounter = InputDelayCounterMax;
	   }
	 //-------------------------------------------------------------------------------------	   
	   public void AddBlockShape( BlockShape bs )
	   {		   
		  BlockShapeList[BlockShapeListCount] = bs;
		  BlockShapeListCount++;
	   }
	 //-------------------------------------------------------------------------------------	   
	   public void OnClick()
	   {
          int i;
          for( i = 0; i < MAX_BUTTONS; i++ )
        	   ButtonList[i].OnClick();
          MultiStateButton_1.OnClick();
	   }
     //-------------------------------------------------------------------------------------		   
	   public void OnKey()
	   {
		  int i;
		  int MSB_State = MultiStateButton_1.GetButtonState();
		  
		  int YesFlag = 0;
		  
		  if( InputDelayCounter == InputDelayCounterMax )
		  {
		      YesFlag = 1;
		      InputDelayCounter = 0;
		  }
		  
		  switch(KeyCode)
		  {
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:	
/*            	
		    	   if( CurrentGameType == GameGlobals.GAME_TYPE_TRADITIONAL )
		    	   {
		    		  if( GameGlobals.TVModeFlag > 0 )
		    		  {
		       		    if( YesFlag > 0 )
            	          MultiStateButton_1.InstantClick();
		    		  }
		    		  else
		            	  BlockShapeList[MSB_State].RotateLeft();
		    	   }
		           else
*/		        	   
		           {
			          for( i = 0; i < BlockShapeListCount; i++ )
			    	    BlockShapeList[i].RotateLeft();	            	            	
		           }
              break;
		    case KeyEvent.KEYCODE_DPAD_UP:		      
/*		    	
	             if( (CurrentGameType == GameGlobals.GAME_TYPE_TRADITIONAL) ||		             
		                 (CurrentGameType == GameGlobals.GAME_TYPE_CHALLENGE ))
	            	 
		    	   if( CurrentGameType == GameGlobals.GAME_TYPE_TRADITIONAL )	
		            	 BlockShapeList[MSB_State].RotateRight();
		             else 		    
*/		            	 
			       for( i = 0; i < BlockShapeListCount; i++ )
			    	   BlockShapeList[i].RotateRight();		
			   break;
		    case KeyEvent.KEYCODE_DPAD_DOWN:
			       for( i = 0; i < BlockShapeListCount; i++ )
			    	    BlockShapeList[i].QuickFall();		    	
			   break;
		    case KeyEvent.KEYCODE_DPAD_LEFT:	
/*		    	
		    	if( CurrentGameType == GameGlobals.GAME_TYPE_TRADITIONAL )	
		        {
		          BlockShapeList[MSB_State].MoveLeft();
		        }		    	
		    	else
*/		    		
			     for( i = 0; i < BlockShapeListCount; i++ )
			    	  BlockShapeList[i].MoveLeft();		    	
			   break;
		    case KeyEvent.KEYCODE_DPAD_RIGHT:
/*		    	
		    	 if( CurrentGameType == GameGlobals.GAME_TYPE_TRADITIONAL )	
		    	 {
		            	 BlockShapeList[MSB_State].MoveRight();
		    	  }
		    	 else
*/		    		 
			     for( i = 0; i < BlockShapeListCount; i++ )
			    	  BlockShapeList[i].MoveRight();	
			   break;    
		    case KeyEvent.KEYCODE_D:
/*		    	
		    	 if( CurrentGameType == GameGlobals.GAME_TYPE_TRADITIONAL )	
		    	  {
		             BlockShapeList[MSB_State].RotateLeft();
		    	  }
		    	 else	
*/		    	 	    	
			       for( i = 0; i < BlockShapeListCount; i++ )
			    	   BlockShapeList[i].RotateLeft();	
			   break;   
		    case KeyEvent.KEYCODE_F:
/*		    	
		    	 if( CurrentGameType == GameGlobals.GAME_TYPE_TRADITIONAL )	
		    	 {
		            	 BlockShapeList[MSB_State].RotateRight();
		    	  }
		    	 else	
*/		    		 
			       for( i = 0; i < BlockShapeListCount; i++ )
			    	   BlockShapeList[i].RotateRight();				       
			   break;			   
		  }
	    }
		//-------------------------------------------------------------------------------------		
		public void Draw()
		{
			int k;
			
		    if( GameGlobals.TVModeFlag == 0 )
			  for( k = 0; k < MAX_BUTTONS; k++ )
			    ButtonList[k].Draw();	
/*			  
	      if( CurrentGameType != GameGlobals.GAME_TYPE_ORIGINAL )	  
	      if( CurrentGameType == GameGlobals.GAME_TYPE_TRADITIONAL )	  
			  MultiStateButton_1.Draw();  
*/			  
		}
		//-------------------------------------------------------------------------------------		
		public void Do()
		{
		  int k, i;
		  Button but;
		  
		  if( InputDelayCounter < InputDelayCounterMax )
		      InputDelayCounter++;
		  		  
		  int MSB_State = MultiStateButton_1.GetButtonState();
		  MultiStateButton_1.Do();
		  
		  for( k = 0; k < MAX_BUTTONS; k++ )
		  {
		    UniqueId = ButtonList[k].UniqueId;
		    but = ButtonList[k];
		    but.Do();

		    if( but.MouseStatus_Dup == but.ME_PRESS_DOWN ||
		    	but.MouseStatus_Dup == but.ME_MOVE )
		    { 
		      switch(UniqueId)
		      {		  
		         case UNIQUE_ID_BUTTON_ROTATE_LEFT:
/*		        	 
		             if( (CurrentGameType == GameGlobals.GAME_TYPE_TRADITIONAL) ||		             
		                 (CurrentGameType == GameGlobals.GAME_TYPE_CHALLENGE ))

		        	 if( CurrentGameType == GameGlobals.GAME_TYPE_TRADITIONAL )		        	 
		             {		            	
		            	 BlockShapeList[MSB_State].RotateLeft();
		             }
		             else
*/		             
				       for( i = 0; i < BlockShapeListCount; i++ )
				    	   BlockShapeList[i].RotateLeft();	
				       break;
		         case UNIQUE_ID_BUTTON_ROTATE_RIGHT:

/*		        	 
		             if( (CurrentGameType == GameGlobals.GAME_TYPE_TRADITIONAL) ||		             
			             (CurrentGameType == GameGlobals.GAME_TYPE_CHALLENGE ))		            	 
		        	   if( CurrentGameType == GameGlobals.GAME_TYPE_TRADITIONAL )		        	 
			             {		            	
			            	 BlockShapeList[MSB_State].RotateRight();
			             }
			             else 		
*/			                     	 
				       for( i = 0; i < BlockShapeListCount; i++ )
				    	   BlockShapeList[i].RotateRight();
				       break;
		         case UNIQUE_ID_BUTTON_LEFT:
/*		        	 
			    	 if( CurrentGameType == GameGlobals.GAME_TYPE_TRADITIONAL )	
			    	 {
			            	 BlockShapeList[MSB_State].MoveLeft();
			    	  }
			    	 else	
*/			    		 
				       for( i = 0; i < BlockShapeListCount; i++ )
				    	   BlockShapeList[i].MoveLeft();			        	 
				       break;
		         case UNIQUE_ID_BUTTON_RIGHT:
/*		        	 
			    	 if( CurrentGameType == GameGlobals.GAME_TYPE_TRADITIONAL )	
			    	  {
			            	 BlockShapeList[MSB_State].MoveRight();
			    	  }
			    	 else		        	
*/			    		 
				       for( i = 0; i < BlockShapeListCount; i++ )
				    	   BlockShapeList[i].MoveRight();	
				       break;
		         case UNIQUE_ID_BUTTON_DROP:
				       for( i = 0; i < BlockShapeListCount; i++ )
				    	    BlockShapeList[i].QuickFall();
				       break;
		         default:
		           break;
		      }
		    }
		    else
		    if( but.MouseStatus_Dup == but.ME_RELEASE )
		    {
		       but.ClearDupInput();	
		    }		  
		    but.ClearDupInput();	
		}
		  
	   if( MultiStateButton_1.MouseStatus_Dup == GameObject.ME_PRESS_DOWN )
	   {
		   
	   }
	   else
	   if( MultiStateButton_1.MouseStatus_Dup == GameObject.ME_RELEASE )
	   {
		   
	   }
		    	
 
			  
	   }
	//-------------------------------------------------------------------------------------			
	}
	//-------------------------------------------------------------------------------------
	class PlayField extends GameObject
	{		
	  public final int FIELD_GRID_WIDTH = BlockGameEngine.FIELD_GRID_WIDTH;
	  public final int FIELD_GRID_HEIGHT = BlockGameEngine.FIELD_GRID_HEIGHT;
      public static final int MAX_LINES_FORMED_COUNT = 10;
	  public char[][] BlockData;

      public int DrawStyle;
      public int ArrayIndex;      
      protected int[] ColorList;
      
      public int LineClearAnimationCounter = 255;
      public int ClearLinesFlag = 0;
      public final static int LineClearAnimationCounterMax = 255;
      public final static int LineClearAnimationCounterIncrement = 25;  
      public int[] LinesYClearArray = null;
      public int[] LinesYClearArrayFullSize = new int[FIELD_GRID_HEIGHT];
      
      int[][] CellClearPlayField = new int[FIELD_GRID_WIDTH][FIELD_GRID_HEIGHT];
      int CellClearCount = 0;
      public final int LEFT_WALL_X = 0 + 2;
      public final int RIGHT_WALL_X = FIELD_GRID_WIDTH - 1 - 2;
      public final int FLOOR_Y1 = FIELD_GRID_HEIGHT - 1;
      public final static int WALL_CELL = 8;
      
      public final int PIT_LEFT_X_PIXELS = LEFT_WALL_X * CELL_WIDTH_PIXELS + CELL_WIDTH_PIXELS;
      public final int PIT_RIGHT_X_PIXELS = RIGHT_WALL_X * CELL_WIDTH_PIXELS;
      public final int PIT_FLOOR_Y1_PIXELS = FLOOR_Y1 * CELL_HEIGHT_PIXELS;
      public final int PIT_BACK_COLOR = Color.rgb(0,0,0);
      
      public int ShakeCounter = 0;
      public static final int SHAKE_COUNTER_MAX = 15;
      public static final int SHAKE_PIXELS = 3;
      public int X_shake = 0, Y_shake = 0;
      
      public int JigglePlayFieldCounter = 0;
      public int JIGGLE_PLAY_FIELD_COUNTER_MAX = 999;
      public int JigglePlayFieldCounterInterval = 20;
      public int[][] PlayFieldJiggleX = new int[FIELD_GRID_WIDTH][FIELD_GRID_HEIGHT];
      public int[][] PlayFieldJiggleY = new int[FIELD_GRID_WIDTH][FIELD_GRID_HEIGHT];
      
	//-------------------------------------------------------------------------------------
      //*** for Game Mode B ***
      
      public int PIT_WIDTH = RIGHT_WALL_X - LEFT_WALL_X - 1;
      public int WIDE_PIT_WIDTH = PIT_WIDTH * 3;
      public final static int MAX_WIDE_PIT_WIDTH = 100;

      public char[][] WidePlayField = new char[MAX_WIDE_PIT_WIDTH][FIELD_GRID_HEIGHT]; 
      public int WidePlayField_View_Window_Start_X = 0;
      public int WidePlayField_Speed = 100;
      public int WidePlayField_Counter = 1;
      
	//-------------------------------------------------------------------------------------
	  public PlayField()
	  {		 
		super();		
		
	    BlockData = new char[FIELD_GRID_WIDTH][FIELD_GRID_HEIGHT];	    
	    int x, y;
	    
        // *** create the boundaries ***
	    for( y = 0; y < FIELD_GRID_HEIGHT; y++ )
	    {
	      BlockData[LEFT_WALL_X][y] = WALL_CELL;  	
	      BlockData[RIGHT_WALL_X][y] = WALL_CELL; 
	    }
	    for( x = LEFT_WALL_X; x <= RIGHT_WALL_X; x++ )
	    	 BlockData[x][FLOOR_Y1] = WALL_CELL;
	    
	    DrawStyle = STYLE_PICTURE;

      ColorList = new int[MAX_SHAPE_TYPES + 1];
      
      int i;
      for( i = 0; i < MAX_SHAPE_TYPES + 1; i++ )
        ColorList[i] = Color.rgb(i * 10 + 10, i * 10 + 10, i * 10 + 10);
     
      ColorList[0] = Color.BLACK;
      ColorList[1] = Color.RED;
      ColorList[2] = Color.BLUE;
      ColorList[3] = Color.GREEN;
      ColorList[4] = Color.CYAN;
      ColorList[5] = Color.MAGENTA;
      ColorList[6] = Color.LTGRAY;      
      ColorList[7] = Color.YELLOW;    
      ArrayIndex = 0;
      
      if( GameGlobals.SpecialEffectsFlag >= 1 )
          DoJigglePlayField(0);
      
      if( GameGlobals.CurrentGameType == GameGlobals.GAME_MODE_B )
          InitWidePlayField();
	  }
	//-------------------------------------------------------------------------------------
	  public int CheckFormLines()
	  {	
        int[] LinesY = new int[MAX_LINES_FORMED_COUNT];
        int LinesYCount;
        int x, y, i, CellCount;
        int LINE_LENGTH = RIGHT_WALL_X - LEFT_WALL_X - 1;
        
        LinesYCount = 0;
        
        for( y = FLOOR_Y1 - 1; y > 0; y-- )
        {
          CellCount = 0; 
          
          for( x = LEFT_WALL_X + 1; x <= RIGHT_WALL_X - 1; x++ )
             if( BlockData[x][y] > 0 )
                 CellCount++;

          if( CellCount >= LINE_LENGTH )
          {            
            LinesY[LinesYCount] = y;          
            LinesYCount = LinesYCount + 1;
          }                                 
        }
        
        for( i = 0; i < LinesYCount; i++ )
         for( x = LEFT_WALL_X + 1; x <= RIGHT_WALL_X - 1; x++ )
           BlockData[x][ LinesY[i]] = 0;
           
        for( i = 0; i < LinesYCount; i++ )
         for( y = FLOOR_Y1 - 1; y >= 1; y-- )
         { 
          CellCount = 0;
      
          for( x = LEFT_WALL_X + 1; x <= RIGHT_WALL_X - 1; x++ )
           if( BlockData[x][y] > 0 )
              CellCount++;
      
          if( CellCount == 0 )
          {
            for( x = LEFT_WALL_X + 1; x <= RIGHT_WALL_X - 1; x++ )
            {
              BlockData[x][y] = BlockData[x][y-1];
              BlockData[x][y-1] = 0;
            }
          }
      
         }          
                
        if( LinesYCount > 0 )
           PlaySound(2);
        
        GameGlobals.Add2CurrentScore( LinesYCount );
        
        if( LinesYCount > 0 )
        	SetSpeed();
      
        return LinesYCount;
	  }
	//-------------------------------------------------------------------------------------
    public int[] CheckFormLinesB()
    {    
        int[] LinesY = new int[MAX_LINES_FORMED_COUNT];
        int LinesYCount;
        int x, y, i, CellCount;
        int LINE_LENGTH = RIGHT_WALL_X - LEFT_WALL_X - 1;
        
        LinesYCount = 0;
        String str ="";
        
        for( y = FLOOR_Y1 - 1; y > 0; y-- )
        {
          CellCount = 0;
          
          for( x = LEFT_WALL_X + 1; x <= RIGHT_WALL_X - 1; x++ )
             if( BlockData[x][y] > 0 )
                 CellCount++;

          if( CellCount >= LINE_LENGTH )
          {            
            LinesY[LinesYCount] = y;          
            LinesYCount = LinesYCount + 1;
            //str += y + ",";
          }                                 
        } 
        
       for( i = 0; i < FIELD_GRID_HEIGHT; i++ )
        	LinesYClearArrayFullSize[i] = 0;
                
        int[] LinesY_B = new int[LinesYCount];
        
        for( i = 0; i < LinesYCount; i++ )
        {
        	 LinesY_B[i] = LinesY[i];
        	 LinesYClearArrayFullSize[ LinesY_B[i] ] = 1;
        }
      
        return LinesY_B;
    }
	//-------------------------------------------------------------------------------------
    public void ShiftBlocksDown( int[] LinesY )
    {    	
    	int i, x, y, LinesYCount = LinesY.length, CellCount;
    	
        for( i = 0; i < LinesYCount; i++ )
         for( x = LEFT_WALL_X + 1; x <= RIGHT_WALL_X - 1; x++ )
           BlockData[x][ LinesY[i]] = 0;
           
        for( i = 0; i < LinesYCount; i++ )
         for( y = FLOOR_Y1 - 1; y >= 1; y-- )
         { 
          CellCount = 0;
      
          for( x = LEFT_WALL_X + 1; x <= RIGHT_WALL_X - 1; x++ )
           if( BlockData[x][y] > 0 )
              CellCount++;
      
          if( CellCount == 0 )
          {
            for( x = LEFT_WALL_X + 1; x <= RIGHT_WALL_X - 1; x++ )
            {
              BlockData[x][y] = BlockData[x][y-1];
              BlockData[x][y-1] = 0;
            }
          }
      
         }          
                
        if( LinesYCount > 0 )
           PlaySound(2);
        
        GameGlobals.Add2CurrentScore( LinesYCount );    	
    }
//-------------------------------------------------------------------------------------	
void ShiftBlocksDownPillars( int[][] ClearPlayField, int Wall_X1, int Wall_X2, int Floor_Y1 )
{
  int x, y, i;

  if( GameGlobals.SpecialEffectsFlag >= 1 )
    BlowUpObject.AddWhole( BlockData, ClearPlayField, FIELD_GRID_WIDTH, Floor_Y1 );
  
  for( y = 0; y < Floor_Y1; y++ )
   for( x = Wall_X1 + 1; x < Wall_X2; x++ )
     if( ClearPlayField[x][y] == 1 )
        BlockData[x][y] = 0;

for( i = 0; i < Floor_Y1; i++ )
  for( y = 0; y < Floor_Y1 - 1; y++ )
   for( x = Wall_X1 + 1; x < Wall_X2; x++ )
   {
     if( BlockData[x][y+1] == 0 && BlockData[x][y] != WALL_CELL )
     {
      BlockData[x][y+1] = BlockData[x][y];
      BlockData[x][y] = 0;
     }
   }
  
  CopyPit2WidePlayField();
}
//-------------------------------------------------------------------------------------	
int CheckFormPillars( int[][] ClearPlayField )
{
  int ClearCount = 0;
  int x, y;
  int c1, c2, c3;
  
    for( y = 1; y < FIELD_GRID_HEIGHT - 1; y++ )
      for( x = 1; x < FIELD_GRID_WIDTH - 1; x++ )
           ClearPlayField[x][y] = 0;
  
    for( y = 1; y < FIELD_GRID_HEIGHT - 1; y++ )
      for( x = 1; x < FIELD_GRID_WIDTH - 1; x++ )
      {
        //*** horizontal ***
        c1 = BlockData[x-1][y];
        c2 = BlockData[x][y];
        c3 = BlockData[x+1][y];
        
        if( c2 == WALL_CELL || c2 == 0 )
            continue;
            
        if( c1 == c2 && c2 == c3 )
        {
          ClearPlayField[x-1][y] = 1;
          ClearPlayField[x][y] = 1;
          ClearPlayField[x+1][y] = 1;
        }
   
        //*** vertical ***
        c1 = BlockData[x][y-1];
        c2 = BlockData[x][y];
        c3 = BlockData[x][y+1];
        
        if( c1 == c2 && c2 == c3 )
        {
          ClearPlayField[x][y-1] = 1;
          ClearPlayField[x][y] = 1;
          ClearPlayField[x][y+1] = 1;
        }        
        
        //*** diagonal 1 ***
        c1 = BlockData[x-1][y-1];
        c2 = BlockData[x][y];
        c3 = BlockData[x+1][y+1];
        
        if( c1 == c2 && c2 == c3 )
        {
          ClearPlayField[x-1][y-1] = 1;
          ClearPlayField[x][y] = 1;
          ClearPlayField[x+1][y+1] = 1;
        }        
        
        //*** diagonal 2 ***
        c1 = BlockData[x+1][y-1];
        c2 = BlockData[x][y];
        c3 = BlockData[x-1][y+1];        
        
        if( c1 == c2 && c2 == c3 )
        {
          ClearPlayField[x+1][y-1] = 1;
          ClearPlayField[x][y] = 1;
          ClearPlayField[x-1][y+1] = 1;
        }        
      }
      
    for( y = 1; y < FIELD_GRID_HEIGHT - 1; y++ )
      for( x = 1; x < FIELD_GRID_WIDTH - 1; x++ )
         if( ClearPlayField[x][y] == 1 )
             ClearCount++; 

    if( ClearCount > 0 )
    	PlaySound(3);
    
    return ClearCount;
}    
//-------------------------------------------------------------------------------------
		public void Draw() 
		{		
		  int x, y; 
		  float fX, fY;
          char Cell;
          
	          float WIDTH = CELL_WIDTH_PIXELS/2;
	          float HEIGHT = CELL_HEIGHT_PIXELS/2;

	          float XShift = 0, YShift = 0;
	       
	          if( ArrayIndex == 0 )
	          {
	            XShift = 0; YShift = 0; 
	          }
	          else
	          if( ArrayIndex == 1 )
	          {
	            XShift = 1; YShift = 0; 
	          }
	          else
	          if( ArrayIndex == 2 )
	          {
	            XShift = 0; YShift = 1; 
	          }
	          else
	          if( ArrayIndex == 3 )
	          {
	            XShift = 1; YShift = 1; 
	          }	  
	          XShift *= WIDTH;
	          YShift *= HEIGHT;
		 
		 if( DrawStyle == STYLE_PRIMITIVE )
		 {		 

		  int c_color, c_color_random; 
			  
		  c_color_random = Color.rgb( BGERandom.nextInt(255), BGERandom.nextInt(255), BGERandom.nextInt(255) );
		  
	      for( y = 0; y < FIELD_GRID_HEIGHT; y++ )
	       {
	        if( LinesYClearArrayFullSize[y] > 0 )
	            c_color_random = Color.rgb( BGERandom.nextInt(255), BGERandom.nextInt(255), BGERandom.nextInt(255) );
	    	  
			for( x = 0; x < FIELD_GRID_WIDTH; x++ )
			   if( BlockData[x][y] > 0  )
			   {	 				   
				   fX = x * CELL_WIDTH_PIXELS; 
				   fY = y * CELL_HEIGHT_PIXELS;

			   if( LineClearAnimationCounter < LineClearAnimationCounterMax )			   
			   	   c_color = c_color_random;			   	   			   
			   else
				   c_color = ColorList[BlockData[x][y]];
			   
				 if( ArrayIndex < LayersCap )  
				   GE.DrawBox( fX, fY, fX+CELL_WIDTH_PIXELS, fY+CELL_HEIGHT_PIXELS, GE.LAYER_1, c_color , 
						    GameEngine.G_STYLE_STROKE );
				   
				   GE.DrawBox( fX+XShift, fY+YShift, fX+XShift+WIDTH, fY+YShift+HEIGHT, 
						     GE.LAYER_2, c_color,GameEngine.G_STYLE_FILL );			   

	/*			   
				   GE.DrawBox( fX, fY, fX+CELL_WIDTH_PIXELS, fY+CELL_HEIGHT_PIXELS, GE.LAYER_3, ColorList[BlockData[x][y]], 
						    GameEngine.G_STYLE_FILL );
	*/					    
			   }
	       }
		 }
		 else
	     if( DrawStyle == STYLE_PICTURE )
	     {	    	       
	      	 
	    	 GE.DrawBox( PIT_LEFT_X_PIXELS + X_shake - 1, 0, PIT_RIGHT_X_PIXELS + X_shake, PIT_FLOOR_Y1_PIXELS + Y_shake, 
	    		GE.LAYER_1, PIT_BACK_COLOR,  GameEngine.G_STYLE_FILL );
	    	 				   
	    	 int alpha;
        
	         for( y = 0; y < FIELD_GRID_HEIGHT; y++ )
	         {
	           if( LineClearAnimationCounter < LineClearAnimationCounterMax && LinesYClearArrayFullSize[y] > 0 )	           
	        	{
	        		alpha = 255 - LineClearAnimationCounter;
	        		if( alpha < 0 )
	        			alpha = 0;
	        	}
	            else
	        	    alpha = 255;
	        	
	     		for( x = 0; x < FIELD_GRID_WIDTH; x++ )
	     		{	
	     		   Cell = BlockData[x][y];

	               if( LineClearAnimationCounter < LineClearAnimationCounterMax && CellClearPlayField[x][y] == 1 )	 
	        	   {
	        		alpha = 255 - LineClearAnimationCounter;
	        		if( alpha < 0 )
	        			alpha = 0;
	             	}
	                else
	        	      alpha = 255;
	           
	     		   if( Cell > 0  )
	     		   {
	    			   fX = (GridX + x) * CELL_WIDTH_PIXELS; 
	    			   fY = (GridY + y) * CELL_HEIGHT_PIXELS;
 
                       BlockCell.DrawCellAlpha( (int)(fX + X_shake + PlayFieldJiggleX[x][y]), (int)(fY + Y_shake + PlayFieldJiggleY[x][y]), Cell - 1, (int)alpha );     				
	     		   }
	     		}
	         }
	     }
		 
		}	  
//-------------------------------------------------------------------------------------	 
public void Do()
{
if( ShakeCounter > 0 )
{
 if( ShakeCounter % 3 == 0 )
 {
    X_shake = SHAKE_PIXELS - BGERandom.nextInt(SHAKE_PIXELS*2+1);
    Y_shake = SHAKE_PIXELS - BGERandom.nextInt(SHAKE_PIXELS*2+1);
 }

 ShakeCounter--;

 if( ShakeCounter <= 0 )
 {
   X_shake = Y_shake = 0;
 }
} 

 if( GameGlobals.SpecialEffectsFlag >= 1 )
     DoJigglePlayField(1);
 
 if( LineClearAnimationCounter >= LineClearAnimationCounterMax )
  if( GameGlobals.CurrentGameType == GameGlobals.GAME_MODE_B )
     MoveWidePlayField();

}
//-------------------------------------------------------------------------------------	
public void StartShake()
{
ShakeCounter = SHAKE_COUNTER_MAX;
}	
//-------------------------------------------------------------------------------------	
public void DoJigglePlayField( int option )
{
int x, y;

if( option == 0 )
{
  for( y = 0; y < FIELD_GRID_HEIGHT; y++ )
   for( x = 0; x < FIELD_GRID_WIDTH; x++ )
   {
     PlayFieldJiggleX[x][y] = 0;
     PlayFieldJiggleX[x][y] = 0;
   }
  
  JigglePlayFieldCounter = 0;
}
else
if( option == 1 )
{
   if( JigglePlayFieldCounter > 0 )
    if( JigglePlayFieldCounter % JigglePlayFieldCounterInterval == 0 )
    {
      for( y = 0; y < FIELD_GRID_HEIGHT; y++ )
        for( x = 0; x < FIELD_GRID_WIDTH; x++ )
      {
        if( BlockData[x][y] > 0 && BlockData[x][y] != WALL_CELL )
        {
         if( BGERandom.nextInt(5) == 0 )
         {
           PlayFieldJiggleX[x][y] = 3 - BGERandom.nextInt(7);
           PlayFieldJiggleY[x][y] = 3 - BGERandom.nextInt(7);
         }
         else
         {
           PlayFieldJiggleX[x][y] = 0;
           PlayFieldJiggleY[x][y] = 0;
         }
        }
      }
    }

   if( JigglePlayFieldCounter < JIGGLE_PLAY_FIELD_COUNTER_MAX )
       JigglePlayFieldCounter++;
   else
   if( JigglePlayFieldCounter >= JIGGLE_PLAY_FIELD_COUNTER_MAX)
       JigglePlayFieldCounter = 0;
}

}
//-------------------------------------------------------------------------------------	
public void CopyWidePlayField2Pit()
{
  int ViewWidthRemain = WIDE_PIT_WIDTH - (WidePlayField_View_Window_Start_X + PIT_WIDTH);
  int copywidth_1 = 0, copywidth_2 = 0;
  int i, x, y;

  if( ViewWidthRemain < 0 )
  {
    copywidth_1 = PIT_WIDTH + ViewWidthRemain - 1;
    
    for( y = 0; y < FLOOR_Y1; y++ )
    {
     x = WidePlayField_View_Window_Start_X;
     for( i = 0; i < copywidth_1; i++ )
     {
       BlockData[LEFT_WALL_X + 1 + i][y] = WidePlayField[x][y];
       x++;
     }      
    }

    copywidth_2 = -1 * ViewWidthRemain;

    for( y = 0; y < FLOOR_Y1; y++ )
    {
     x = 0;
     for( i = 0; i < copywidth_2; i++ )
     {
       BlockData[LEFT_WALL_X + 1 + i + copywidth_1][y] = WidePlayField[x][y];
       x++;
     }      
    }      
  }
  else
  {
    for( y = 0; y < FLOOR_Y1; y++ )
    {
     x = WidePlayField_View_Window_Start_X;
     for( i = 0; i < PIT_WIDTH; i++ )
     {
       BlockData[LEFT_WALL_X + 1 + i][y] = WidePlayField[x][y];
       x++;
     }      
    }
  }
}
//-------------------------------------------------------------------------------------
public void CopyPit2WidePlayField()
{
  if( GameGlobals.CurrentGameType != GameGlobals.GAME_MODE_B )
	  return;
  
  int ViewWidthRemain = WIDE_PIT_WIDTH - (WidePlayField_View_Window_Start_X + PIT_WIDTH);
  int copywidth_1 = 0, copywidth_2 = 0;
  int i, x, y;

  if( ViewWidthRemain < 0 )
  {
    copywidth_1 = PIT_WIDTH + ViewWidthRemain - 1;
    
    for( y = 0; y < FLOOR_Y1; y++ )
    {
     x = WidePlayField_View_Window_Start_X;
     for( i = 0; i < copywidth_1; i++ )
     {
       WidePlayField[x][y] = BlockData[LEFT_WALL_X + 1 + i][y];
       x++;
     }      
    }

    copywidth_2 = -1 * ViewWidthRemain;

    for( y = 0; y < FLOOR_Y1; y++ )
    {
     x = 0;
     for( i = 0; i < copywidth_2; i++ )
     {
       WidePlayField[x][y] = BlockData[LEFT_WALL_X + 1 + i + copywidth_1][y];
       x++;
     }      
    }      
  }
  else
  {
    for( y = 0; y < FLOOR_Y1; y++ )
    {
     x = WidePlayField_View_Window_Start_X;
     for( i = 0; i < PIT_WIDTH; i++ )
     {
       WidePlayField[x][y] = BlockData[LEFT_WALL_X + 1 + i][y];
       x++;
     }      
    }
  }
}
//-------------------------------------------------------------------------------------
public void MoveWidePlayField()
{
/*	
      public int PIT_WIDTH = RIGHT_WALL_X - LEFT_WALL_X + 1;
      public int WIDE_PIT_WIDTH = PIT_WIDTH * 3;

      public char[][] WidePlayField = new char[WIDE_PIT_WIDTH][FIELD_GRID_HEIGHT];
      public int WidePlayField_View_Window_Start_X = 0;
      public int WidePlayField_Speed = 20;
      public int WidePlayField_Counter = 0;
*/
   if( WidePlayField_Counter < 999 )
	   WidePlayField_Counter++;
   else   
   if( WidePlayField_Counter >= 999 )
       WidePlayField_Counter = 1;
  
   if( WidePlayField_Counter % WidePlayField_Speed == 0 )
   {
	  WidePlayField_View_Window_Start_X++;
      if( WidePlayField_View_Window_Start_X >= WIDE_PIT_WIDTH )
    	  WidePlayField_View_Window_Start_X = 0;
      
      CopyWidePlayField2Pit();
   }
}
//-------------------------------------------------------------------------------------
public void InitWidePlayField()
{
   int x, y, r;
/*   
   for( y =  FLOOR_Y1 - 5; y < FLOOR_Y1; y++ )
	 for( x = 0; x < WIDE_PIT_WIDTH; x++ )
	 { 
		r = BGERandom.nextInt(4);
		if( r == 0 )
			WidePlayField[x][y] = WALL_CELL;
	 }
*/
  y = FLOOR_Y1 - 1; int flag = 0;
  
	  for( x = 0; x < WIDE_PIT_WIDTH; x++ )  
	  {	  
		y = FLOOR_Y1 - BGERandom.nextInt(5) - 1;
	    WidePlayField[x][y] = WALL_CELL;      
	  } 	
	   
   CopyWidePlayField2Pit();
}
//-------------------------------------------------------------------------------------
	}
	//-------------------------------------------------------------------------------------
    class InfoDisplay
    {	   
	   protected Caption ItsCaption;
	  
	 //-------------------------------------------------------------------------------------
	   public InfoDisplay()
	   {		  
		   
       ItsCaption = new Caption( 250, 148, GameGlobals.GROUP_ID_NONE, GameGlobals.UNIQUE_ID_CAPTION_GENERAL, Caption.STYLE_TEXT, 
    		GameEngine.LAYER_4, GameEngine.LAYER_4 );
       ItsCaption.Init_1( "High Scores", 20, GameGlobals.MainFontColor, 0, 0, true, 40 ); 
		 
       ItsCaption = new Caption( 250, 148, "" );  
		 ItsCaption.SetTextOptions( 20, Color.rgb(255,255,0), 0, 0, true, 40 );
		 ItsCaption.TextLayer = GameEngine.LAYER_4;
		 ItsCaption.UniqueId = GameGlobals.UNIQUE_ID_CAPTION_GENERAL;
		
	   }
	 //-------------------------------------------------------------------------------------
	   public void Draw()
	   {		  
		 String s = new String("");
		 
		 s += "Score\n" + GameGlobals.CurrentPlayerScore + "\n"; 
		 s += "Speed\n" + GameGlobals.GameSpeed + "\n";
		 s += "Cells\n" + GameGlobals.CurrentCellCount + "\n";
		 
		 ItsCaption.SetText(s);
		 ItsCaption.Draw();
		 
	   }
	 //-------------------------------------------------------------------------------------
	   public void Do()
	   {		  
	   }
	 //-------------------------------------------------------------------------------------
    }	
	//-------------------------------------------------------------------------------------		
	  protected static PlayField[] ThePlayField;
	  protected static BlockShape[] ThePlayBlockShape;
	  protected static BlockShape[] TheNextBlockShape;
     	  	  
	  protected PlayerControls ThePlayerControls; // up, down, left, rotate left, rotate right, score, speed, etc.
	  protected InfoDisplay TheInfoDisplay;
	  	  
    //-------------------------------------------------------------------------------------	  
	public BlockGameEngine()
	{		
		super();

		//* * * * * * * * * * * * * * * * * * * * * * *
		
        CurrentGameType = GameGlobals.CurrentGameType;
        LayersCap = 1; //GameGlobals.PitCount;
        MainFallDelay = MAX_FALL_DELAY - GameGlobals.GameSpeed * 2;
        
        //* * * * * * * * * * * * * * * * * * * * * * *
        
        GameOverFlag = 0;
        
		ClassType[TYPE_BLOCK_GAME_ENGINE] = 1;
		Name = new String("BlockGameEngine");
		
		MouseEventNotifyFlag = true;
		KeyEventNotifyFlag = true;		 
		int i;
		
		BGERandom = new Random();
		BagRandomNumber( 0, LayersCap );
					
        ThePlayField = new PlayField[MAX_PLAY_FIELD_LAYERS];
        ThePlayBlockShape = new BlockShape[MAX_PLAY_FIELD_LAYERS];
        TheNextBlockShape = new BlockShape[MAX_PLAY_FIELD_LAYERS];       
        ThePlayerControls = new PlayerControls();
        
        for( i = 0; i < MAX_PLAY_FIELD_LAYERS; i++ )
        {
          ThePlayField[i] = new PlayField();          
          ThePlayBlockShape[i] = new BlockShape();
          TheNextBlockShape[i] = new BlockShape();
          ThePlayField[i].ArrayIndex = i;          
          ThePlayerControls.AddBlockShape(ThePlayBlockShape[i]);
        }               
        
        for( i = 0; i < LayersCap; i++ )
        {
         ThePlayBlockShape[i].Init( 4, -1, ThePlayField[i], false, MainFallDelay, TheNextBlockShape[i], i, ThePlayBlockShape, this ) ;
         TheNextBlockShape[i].Init( 11, 2, ThePlayField[i], true, MainFallDelay, null, i, TheNextBlockShape, this ) ;  
         TheNextBlockShape[i].AlphaValue = BlockShape.NEXT_PIECE_ALPHA_VALUE;
        }
        
        BlockDoneFallingCount = 0;     
/*                        		
   	    if( CurrentGameType == GameGlobals.GAME_TYPE_ORIGINAL )
   	    {
    	    ThePlayBlockShape[0].CopyNextBlockShapeList();        
    	    ThePlayBlockShape[0].ChooseNextBlockLayer();
   	    }	    
   	    else
		if( CurrentGameType == GameGlobals.GAME_TYPE_CHALLENGE )
		{

			//ThePlayBlockShape[0].SharePieceLayers();
			//TheNextBlockShape[0].SharePieceLayers();
			
    	    ThePlayBlockShape[0].CopyNextBlockShapeList();        
    	    ThePlayBlockShape[0].ChooseNextBlockLayer();			
		}	   	    
*/
                
   	    TheInfoDisplay = new InfoDisplay();
/*   	    
   	 if( GameGlobals.GarbageBlocksFlag > 0 )
   		CreateGarbageCells();
*/   	    
   	 if( GameGlobals.LoadGameFileFlag > 0 )
   		RetrieveGameDataGlobals();

     for( i = 0; i < MAX_PLAY_FIELD_LAYERS; i++ )
       QuickFallBlockShapeList[i] = null;    	 
	}
	//-------------------------------------------------------------------------------------
	protected void RetrieveGameDataGlobals()
	{		
		int i, x, y;
        for( i = 0; i < MAX_PLAY_FIELD_LAYERS; i++ )
        {
        	ThePlayBlockShape[i].Id = GameGlobals.GameSaveData[i].BlockShape_Id;
            ThePlayBlockShape[i].GridX = GameGlobals.GameSaveData[i].BlockShape_GridX;
            ThePlayBlockShape[i].GridY = GameGlobals.GameSaveData[i].BlockShape_GridY;
        	ThePlayBlockShape[i].EnabledFlag = GameGlobals.GameSaveData[i].BlockShape_EnabledFlag;
        	
	        for( y = 0; y < SHAPE_HEIGHT; y++ )
		      for( x = 0; x < SHAPE_WIDTH; x++ )  
		    	  ThePlayBlockShape[i].BlockData[x][y] = GameGlobals.GameSaveData[i].BlockShape_BlockData[x][y];

        	TheNextBlockShape[i].Id = GameGlobals.GameSaveData[i].BlockShape_Next_Id;
        	TheNextBlockShape[i].EnabledFlag = GameGlobals.GameSaveData[i].BlockShape_Next_EnabledFlag;
        	
	        for( y = 0; y < SHAPE_HEIGHT; y++ )
		      for( x = 0; x < SHAPE_WIDTH; x++ )  
		    	  TheNextBlockShape[i].BlockData[x][y] = GameGlobals.GameSaveData[i].BlockShape_Next_BlockData[x][y];
	        
	        for( y = 0; y < FIELD_GRID_HEIGHT; y++ )
		      for( x = 0; x < FIELD_GRID_WIDTH; x++ ) 
		    	  ThePlayField[i].BlockData[x][y] = GameGlobals.GameSaveData[i].PlayField_BlockData[x][y];
	        
	        for( y = 0; y < FIELD_GRID_HEIGHT; y++ )
		      for( x = 0; x < BlockGameEngine.PlayField.MAX_WIDE_PIT_WIDTH; x++ ) 	        
  	              ThePlayField[0].WidePlayField[x][y] = GameGlobals.WidePlayField_Data[x][y];
    
            ThePlayField[0].WidePlayField_View_Window_Start_X = GameGlobals.WidePlayField_View_Window_Start_X;  	        
        }
	}
	//-------------------------------------------------------------------------------------	
	public void OnKey() 
	{		
	  ThePlayerControls.OnKey();	
	  ThePlayerControls.ClearDupInput();	  
	}
	//-------------------------------------------------------------------------------------	
	public void OnClick() 
	{		
	 if( GameGlobals.TVModeFlag == 0 )
	 {
	   ThePlayerControls.OnClick();	
	   ThePlayerControls.ClearDupInput();		
	 }
	}
	//-------------------------------------------------------------------------------------	
	public void Do()
	{		
	  int i;
	  String s = new String("");
	  
	  for( i = 0; i < LayersCap; i++ )
	  {
		 ThePlayField[i].Do();
		 
	  if( ThePlayBlockShape[i].EnabledFlag == 1 )	 
		 ThePlayBlockShape[i].Do();
	  if( TheNextBlockShape[i].EnabledFlag == 1 )	  
		 TheNextBlockShape[i].Do();		 

        if( QuickFallBlockShapeList[i] != null )
        {
          QuickFallBlockShapeList[i].Do();	  
          if( QuickFallBlockShapeList[i].ActiveFlag == 0 )
        	  QuickFallBlockShapeList[i] = null;
        }
        
	   //s += ThePlayBlockShape[i].GridX + ", " + ThePlayBlockShape[i].GridY + ", " + ThePlayBlockShape[i].EnabledFlag + "; ";
	  }
/*	  
	  s = "GameSpeed = " + GameGlobals.GameSpeed;
	  s += "\nPitCount = " + GameGlobals.PitCount;
	  s += "\nSoundsFlag = " + GameGlobals.SoundsFlag;
	  s += "\nGarbageBlocksFlag = " + GameGlobals.GarbageBlocksFlag;
	  s += "\nBackGroundIndex = " + GameGlobals.BackGroundIndex;
	  s += "\nWorkingBackGroundIndex = " + GameGlobals.WorkingBackGroundIndex;
*/	  
	  //GameGlobals.debugcaption.SetText(s);
	  
	  ThePlayerControls.Do();
	  BlowUpObject.Do();
	}
	//-------------------------------------------------------------------------------------	
	public void Draw() 
	{
		
	  int i;
	  for( i = 0 ; i < LayersCap; i++ )
	  {
		  if( ThePlayBlockShape[i].EnabledFlag == 1 )		  
  		   ThePlayBlockShape[i].Draw();
		  if( TheNextBlockShape[i].EnabledFlag == 1 )		  
   		   TheNextBlockShape[i].Draw();
       if( QuickFallBlockShapeList[i] != null )
           QuickFallBlockShapeList[i].Draw();			  
	  }
	  
	  for( i = 0; i < LayersCap /* MAX_PLAY_FIELD_LAYERS */; i++ )
		   ThePlayField[i].Draw();		 
	  
	      ThePlayerControls.Draw();
	  
	  TheInfoDisplay.Draw();
	  BlowUpObject.Draw();
	}
	//-------------------------------------------------------------------------------------	   
	public void SaveGameData()
	{		
	  int i;
	  
	  for( i = 0; i < MAX_PLAY_FIELD_LAYERS; i++ )		 
	     GameGlobals.SetPlayData( i, 
	    		 ThePlayBlockShape[i].Id,
	    		 ThePlayBlockShape[i].GridX,
	    		 ThePlayBlockShape[i].GridY,
	    		 ThePlayBlockShape[i].BlockData,
	    		 ThePlayBlockShape[i].EnabledFlag,       
	    		 TheNextBlockShape[i].Id,
	    		 TheNextBlockShape[i].BlockData,
	    		 TheNextBlockShape[i].EnabledFlag,       
	    		 ThePlayField[i].BlockData ); 	
	  
	  GameGlobals.SetWidePlayFieldData( ThePlayField[0].WidePlayField, ThePlayField[0].WidePlayField_View_Window_Start_X );
	  
	  GameGlobals.SaveGameData();
	}
	//-------------------------------------------------------------------------------------	
	protected void CreateGarbageCells()
	{
	  char Cell;
	  int lower, upper, x, y;
	  int i;
	  
	  int StartY, EndY, StartX, EndX;
	    
	  upper = ThePlayField[0].FLOOR_Y1 - 5;
	  lower = ThePlayField[0].FLOOR_Y1 - 10;
	  
	  StartY = BGERandom.nextInt(upper - lower + 1) + lower;
	  EndY = ThePlayField[0].FLOOR_Y1 - 1;
	  StartX = ThePlayField[0].LEFT_WALL_X + 1; EndX = ThePlayField[0].RIGHT_WALL_X - 1;

	  if( LayersCap > 1 )
	  {
	    lower = 1;
	    upper = GameGlobals.CellTypeCount; // LayersCap;
	  }
	  else
	  if( LayersCap == 1 )
	  {
	    lower = 1;
	    upper = GameGlobals.CellTypeCount; // 7;
	  }
	  
	  for( i = 0; i < 70; i++ )
	  {
	     Cell = (char)(BGERandom.nextInt(upper - lower + 1) + lower);
	     x = BGERandom.nextInt(EndX - StartX + 1) + StartX;
	     y = BGERandom.nextInt(EndY - StartY + 1) + StartY;
	     	    
	     if( LayersCap > 1 )
	       ThePlayField[Cell - 1].BlockData[x][y] = Cell;
	     else
	     if( LayersCap == 1 )
	       ThePlayField[0].BlockData[x][y] = Cell;
	  }
	  
	}	
	//-------------------------------------------------------------------------------------
	protected void PlaySound( int Index )
	{
		if( GameGlobals.SoundsFlag > 0 )
		{
		   GE.SoundOperationFlag = 1;
		   GE.SoundPlayIndex = Index; 
		}		
	}
	//-------------------------------------------------------------------------------------	
	public static void SetSpeed()
	{
	  MainFallDelay = MAX_FALL_DELAY - GameGlobals.GameSpeed * 2;		
	}	
//-------------------------------------------------------------------------------------	
public void CreateFallWoosh( int Index, BlockShape src, int GridY_Top, int GridY_Bottom )
{
   QuickFallBlockShapeList[Index] = new QuickFallBlockShape();
   QuickFallBlockShapeList[Index].CopyBlockShapeData( QuickFallBlockShapeList[Index], src );
   QuickFallBlockShapeList[Index].Init( GridY_Top, GridY_Bottom );
}	    
//#####################################################################################
class QuickFallBlockShape extends BlockShape
{
public final static int LIFE_COUNTER_MAX = 10;
protected int LifeCounter = LIFE_COUNTER_MAX;
public int ActiveFlag = 1;
protected int Y_Top, Y_Bottom;

//-------------------------------------------------------------------------------------
public void Init( int nY_Top, int nY_Bottom )
{
	if( nY_Top < 0 )
		nY_Top = 0;
	
	Y_Top = nY_Top; Y_Bottom = nY_Bottom;
}
//-------------------------------------------------------------------------------------
public void Do()
{
   if( LifeCounter <= 0 )
     ActiveFlag = 0;
   else
     LifeCounter--;
   
}
//-------------------------------------------------------------------------------------
public boolean Fall() { return false; }
public void GroupRebirth() {}
public void Rebirth() {} 
//-------------------------------------------------------------------------------------
public void Draw()
{
	  int x, y, i;
	  float fX, fY, alpha = 255f, alpha_percent = 0.90f;
      char Cell;           

  for( i = Y_Bottom; i >= Y_Top; i-- )
  {  
     GridY = i;
     alpha = alpha * alpha_percent * (LifeCounter+0.0f)/LIFE_COUNTER_MAX;
     
         for( y = 0; y < SHAPE_HEIGHT; y++ )
     		for( x = 0; x < SHAPE_WIDTH; x++ )
     		   if( BlockData[x][y] > 0  )
     		   {     			   
	     		   Cell = BlockData[x][y];
	     		   
	     		   if( Cell > 0  )
	     		   {
	    			   fX = (GridX + x) * CELL_WIDTH_PIXELS; 
	    			   fY = (GridY + y) * CELL_HEIGHT_PIXELS;
	    			   
                      BlockCell.DrawCellAlpha( (int)fX, (int)fY, Cell - 1, (int)alpha );    			        				
	     		   }     			
                }
   }

}	
}
//#####################################################################################	
}	