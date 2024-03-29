package skittles.sim;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Game 
{
	private Player[] aplyPlayers;
	private PlayerStatus[] aplsPlayerStatus;
	private int intPlayerNum;
	private int intColorNum;
	public Visualization viz = new Visualization();
	private Offer[] aoffCurrentOffers = null;
	private int[][] aintCurrentEats = null;
	
	private double dblTasteMean;
	
	public static Scanner scnInput = new Scanner( System.in );
	
	public Game( String strXMLPath )
	{
		DocumentBuilderFactory dbfGameConfig = DocumentBuilderFactory.newInstance();

		Document dcmGameConfig = null;
		try 
		{
			//Using factory get an instance of document builder
			DocumentBuilder dbdGameConfig = dbfGameConfig.newDocumentBuilder();
			//parse using builder to get DOM representation of the XML file
			dcmGameConfig = dbdGameConfig.parse( strXMLPath );
		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch(SAXException se) {
			se.printStackTrace();
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}
		//get the root element
		dcmGameConfig.getDocumentElement().normalize();
		//get a nodelist of elements
		NodeList ndlGame = dcmGameConfig.getElementsByTagName("Game");
		int intTotalNum = 0;
		if(ndlGame != null && ndlGame.getLength() > 0) 
		{
			for(int i = 0 ; i < ndlGame.getLength();i++) 
			{
				//get the employee element
				Element elmGame = (Element) ndlGame.item(i);
				//retrieve player information
				intColorNum = Integer.parseInt( getTagValue( elmGame, "ColorNum" ) );
				intTotalNum = Integer.parseInt( getTagValue( elmGame, "SkittleNum" ) );	
			}
		}
		// initialize players
		ArrayList< Player > alPlayers = new ArrayList< Player >();			// players
		ArrayList< PlayerStatus > alPlayerStatus = new ArrayList< PlayerStatus >();		// status of players for simulator's record
		//get a nodelist of elements
		NodeList ndlPlayers = dcmGameConfig.getElementsByTagName("Player");
		if(ndlPlayers != null && ndlPlayers.getLength() > 0) 
		{
			intPlayerNum = ndlPlayers.getLength();
			for(int i = 0 ; i < ndlPlayers.getLength();i++) 
			{

				//get the employee element
				Element elmPlayer = (Element) ndlPlayers.item(i);
				//retrieve player information
				String strPlayerClass = getTagValue( elmPlayer, "Class" );
				String strTastes = getTagValue( elmPlayer, "Happiness" );
				String[] astrTastes = strTastes.split( "," );
				double[] adblTastes = new double[ intColorNum ];
				if ( !astrTastes[ 0 ].equals( "random" ) )
				{
					for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
					{
						adblTastes[ intColorIndex ] = Double.parseDouble( astrTastes[ intColorIndex ] );
					}
				}
				else
				{
					double dblMean = Double.parseDouble( astrTastes[ 1 ] );
					this.dblTasteMean = dblMean;
					adblTastes = randomTastes( dblMean );
					System.out.println( "Random color happiness:" );
					for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
					{
						System.out.print( adblTastes[ intColorIndex ] + " " );
					}
					System.out.println();
				}
				String strInHand = getTagValue( elmPlayer, "InHand" );
				int[] aintInHand = new int[ intColorNum ];
				int intTempSkittleCount = 0;
				if ( strInHand.equals( "random" ) )
				{
					aintInHand = randomInHand( intTotalNum );
					for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
					{
						intTempSkittleCount += aintInHand[ intColorIndex ];
					}
				}
				else
				{
					String[] astrInHand = strInHand.split( "," );
					for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
					{
						aintInHand[ intColorIndex ] = Integer.parseInt( astrInHand[ intColorIndex ] );
						intTempSkittleCount += aintInHand[ intColorIndex ];
					}
					if ( intTempSkittleCount != intTotalNum )
					{
						System.out.println( "Skittle number in hand is not consistent." );
					}
				}
				Player plyNew = null;
				try {
					plyNew = ( Player ) Class.forName( strPlayerClass ).newInstance();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				plyNew.initialize( intPlayerNum, dblTasteMean, i, strPlayerClass, aintInHand.clone() );
				alPlayers.add( plyNew );
				PlayerStatus plsTemp = new PlayerStatus( i, strPlayerClass, aintInHand.clone(), adblTastes.clone() );
				alPlayerStatus.add( plsTemp );
			}
		}
		aplyPlayers = alPlayers.toArray( new Player[ 0 ] );
		aplsPlayerStatus = alPlayerStatus.toArray( new PlayerStatus[ 0 ] );	
	}
	
	public void runGame()
	{
		FileWriter[] afrtPortfolio = new FileWriter[ intPlayerNum ];
		BufferedWriter[] abfwPortfolio = new BufferedWriter[ intPlayerNum ];
		try {
			for ( int intPlayerIndex = 0; intPlayerIndex < intPlayerNum; intPlayerIndex ++ )
			{
				afrtPortfolio[ intPlayerIndex ] = new FileWriter( "P" + intPlayerIndex + ".txt" );
				abfwPortfolio[ intPlayerIndex ] = new BufferedWriter( afrtPortfolio[ intPlayerIndex ] );
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// check whether there is still at least one player has more skittles to eat
		logGame( abfwPortfolio, "E" );
		logGame( abfwPortfolio, "O" );
		logGame( abfwPortfolio, "P" );
		logGame( abfwPortfolio, "H" );
		logGame( abfwPortfolio, "N" );
		int time = 0;
		viz.setPlayers(this.aplyPlayers);
		while ( !checkFinish() )
		{
			showEveryInHand();		
			everyoneEatAndOffer();
			logGame( abfwPortfolio, "E" );
			viz.updateStatuses(time, aplsPlayerStatus);
			int[] aintOrder = generateRandomOfferPickOrder();			// need code to log the order for repeated game
			pickOfferInOrder( aintOrder );
			viz.updateOffers(time++, aoffCurrentOffers);
			broadcastOfferExcution();
			logGame( abfwPortfolio, "O" );
			logGame( abfwPortfolio, "P" );
			logGame( abfwPortfolio, "H" );
			logGame( abfwPortfolio, "N" );
		}
		double dblTotal = 0;
		for ( PlayerStatus plsTemp : aplsPlayerStatus )
		{
			dblTotal += plsTemp.getHappiness();
		}
		for ( PlayerStatus plsTemp : aplsPlayerStatus )
		{
			double dblTempHappy = ( plsTemp.getHappiness() + ( dblTotal - plsTemp.getHappiness() ) / ( intPlayerNum - 1 ) ) / 2;
			System.out.println( "Player #" + plsTemp.getPlayerIndex() + "'s happiness is: " + dblTempHappy );
		}
		
		try {
			for ( int intPlayerIndex = 0; intPlayerIndex < intPlayerNum; intPlayerIndex ++ )
			{
				abfwPortfolio[ intPlayerIndex ].close();
				afrtPortfolio[ intPlayerIndex ].close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		viz.generateGraphs();
		try{
		    Scanner in = new Scanner(new File("VIZ_CONFIG.txt"));
	//	    viz.generateHTML(in.nextLine().trim());
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(-1);
		}
		
	}
	
	private void logGame( BufferedWriter[] abfwPortfolio, String strLogWhat )
	{
		if ( strLogWhat.equals( "P" ) )
		{
			for ( int intPlayerIndex = 0; intPlayerIndex < intPlayerNum; intPlayerIndex ++ )
			{
				PlayerStatus plsTemp = aplsPlayerStatus[ intPlayerIndex ];
				int[] aintInHand = plsTemp.getInHand();
				for ( int intInHand : aintInHand )
				{
					try {
						abfwPortfolio[ intPlayerIndex ].write( intInHand + "\t" );
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		else if ( strLogWhat.equals( "E" ) )
		{
			for ( int intPlayerIndex = 0; intPlayerIndex < intPlayerNum; intPlayerIndex ++ )
			{
				int[] aintCurrentEat;
				if ( aintCurrentEats == null )
				{
					aintCurrentEat = new int[ intColorNum ];
				}
				else
				{
					aintCurrentEat = aintCurrentEats[ intPlayerIndex ];
				}
				for ( int intEat : aintCurrentEat )
				{
					try {
						abfwPortfolio[ intPlayerIndex ].write( intEat + "\t" );
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		else if ( strLogWhat.equals( "N" ) )
		{
			for ( int intPlayerIndex = 0; intPlayerIndex < intPlayerNum; intPlayerIndex ++ )
			{
				try {
					abfwPortfolio[ intPlayerIndex ].write( "\n" );
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else if ( strLogWhat.equals( "O" ) )
		{
			int[][] aintSumOffExe = new int[ intPlayerNum ][ intColorNum ];
			int[][] aintOffs = new int[ intPlayerNum ][ intColorNum ];
			if ( aoffCurrentOffers != null )
			{
				for ( Offer offTemp : aoffCurrentOffers )
				{
					int[] aintOff = offTemp.getOffer();
					int[] aintDesire = offTemp.getDesire();
					int intOfferedByIndex = offTemp.getOfferedByIndex();
					int intPickedByIndex = offTemp.getPickedByIndex();
					for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
					{
						aintOffs[ intOfferedByIndex ][ intColorIndex ] = aintOff[ intColorIndex ] - aintDesire[ intColorIndex ];
					}
					if ( !offTemp.getOfferLive() && offTemp.getPickedByIndex() != -1 )
					{
						for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
						{
							aintSumOffExe[ intOfferedByIndex ][ intColorIndex ] += aintDesire[ intColorIndex ] - aintOff[ intColorIndex ];
							aintSumOffExe[ intPickedByIndex ][ intColorIndex ] += aintOff[ intColorIndex ] - aintDesire[ intColorIndex ];
						}
					}
				}
			}
			for ( int intPlayerIndex = 0; intPlayerIndex < intPlayerNum; intPlayerIndex ++ )
			{
				int[] aintSumOffE = aintSumOffExe[ intPlayerIndex ];
				int[] aintOff = aintOffs[ intPlayerIndex ];
				for ( int intOff : aintOff )
				{
					try {
						abfwPortfolio[ intPlayerIndex ].write( intOff + "\t" );
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				for ( int intSumOffE : aintSumOffE )
				{
					try {
						abfwPortfolio[ intPlayerIndex ].write( intSumOffE + "\t" );
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		else if ( strLogWhat.equals( "H" ) )
		{
			for ( int intPlayerIndex = 0; intPlayerIndex < intPlayerNum; intPlayerIndex ++ )
			{
				PlayerStatus plsTemp = aplsPlayerStatus[ intPlayerIndex ];
				double dblHappiness = plsTemp.getHappiness();
				try {
					abfwPortfolio[ intPlayerIndex ].write( dblHappiness + "\t" );
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private double[] randomTastes(double dblMean) 
	{
		double[] adblRandomTastes = new double[ intColorNum ];
		Random rdmTemp = new Random();
		for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
		{
			double dblTemp = -5;		// out of range [ -1, 1 ]
			while ( dblTemp < -1 || dblTemp > 1 )
			{
				dblTemp = rdmTemp.nextGaussian() + dblMean;
			}
			adblRandomTastes[ intColorIndex ] = dblTemp;
		}
		return adblRandomTastes;
	}

	private int[] randomInHand(int intTotalNum) 
	{
		int[] aintRandomInHand = new int[ intColorNum ];
//		Random rdmTemp = new Random();
//		int[] aintTemp = new int[ intColorNum + 1 ];
//		aintTemp[ intColorNum ] = intTotalNum;
//		for ( int intColorIndex = 1; intColorIndex < intColorNum; intColorIndex ++ )
//		{
//			aintTemp[ intColorIndex ] = rdmTemp.nextInt( intTotalNum + 1 );
//		}
//		Arrays.sort( aintTemp );
////		System.out.println( "RandomInHand: " );
//		for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
//		{
//			aintRandomInHand[ intColorIndex ] = aintTemp[ intColorIndex + 1 ] - aintTemp[ intColorIndex ];
////			System.out.print( aintRandomInHand[ intColorIndex ] + " " );
//		}
//		System.out.println();
		Random rdmTemp = new Random();
		for ( int intSkittleIndex = 0; intSkittleIndex < intTotalNum; intSkittleIndex ++ )
		{
			int intTemp = rdmTemp.nextInt( intColorNum );
			aintRandomInHand[ intTemp ] ++;
		}
		return aintRandomInHand;
	}

	private String getTagValue( Element elmPlayer, String strTagName )
	{
		String strValue = null;
		NodeList ndlPlayer = elmPlayer.getElementsByTagName( strTagName );
		if( ndlPlayer != null && ndlPlayer.getLength() > 0) {
			Element elmTag = (Element) ndlPlayer.item(0);
			strValue = elmTag.getFirstChild().getNodeValue();
		}
		return strValue;
	}
	
	private void showEveryInHand() 
	{
		System.out.println( "<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n" );
		System.out.println( "******************************************" );
		System.out.println( "------------------------------------------");
		System.out.println( "Skittles portfolio:" );
		for ( PlayerStatus plsTemp : aplsPlayerStatus )
		{
			System.out.println( plsTemp.toString() );
		}
		System.out.println( "------------------------------------------");
		System.out.println( "******************************************\n" );
	}

	private boolean checkFinish()
	{
		boolean blnFinish = true;
		for ( int intPlayerIndex = 0; intPlayerIndex < intPlayerNum; intPlayerIndex ++ )
		{
			if ( aplsPlayerStatus[ intPlayerIndex ].getTotalInHand() > 0 )
			{
				blnFinish = false;
				break;
			}
		}
		return blnFinish;
	}

	private void everyoneEatAndOffer()
	{
		ArrayList< Offer > alCurrentOffers = new ArrayList< Offer >();
		ArrayList< int[] > alEats = new ArrayList< int[] >();
		for ( int intPlayerIndex = 0; intPlayerIndex < intPlayerNum; intPlayerIndex ++ )
		{
			// skip the player who has eaten all the skittles
			int[] aintTempEat = new int[ intColorNum ];
			Offer offTemp = new Offer( intPlayerIndex, intColorNum );
			if ( aplsPlayerStatus[ intPlayerIndex ].getTotalInHand() == 0 )
			{
				alEats.add( aintTempEat );
				alCurrentOffers.add( offTemp );
				continue;
			}
			aplyPlayers[ intPlayerIndex ].eat( aintTempEat );
			// process eat
			if ( aplsPlayerStatus[ intPlayerIndex ].checkCanEat( aintTempEat ) )
			{
				double dblHappinessUp = aplsPlayerStatus[ intPlayerIndex ].eat( aintTempEat );
				alEats.add( aintTempEat );
				aplyPlayers[ intPlayerIndex ].happier( dblHappinessUp );
			}
			else
			{
				double dblHappinessUp = aplsPlayerStatus[ intPlayerIndex ].randEat( aintTempEat );
				alEats.add( aintTempEat );
				aplyPlayers[ intPlayerIndex ].happier( dblHappinessUp );
				System.out.println( "Player #" + intPlayerIndex + ": You cannot eat these. Take them out of your mouth!" );
			}
			// process offer
			aplyPlayers[ intPlayerIndex ].offer( offTemp );
			if ( aplsPlayerStatus[ intPlayerIndex ].checkValidOffer( offTemp ) )
			{
				alCurrentOffers.add( offTemp );
			}
			else
			{
				System.out.println( "Player #" + intPlayerIndex + ": Invalid offer. Shame on you :)" );
				Offer offEmpty = new Offer( intPlayerIndex, intColorNum );
				alCurrentOffers.add( offEmpty );
			}
		}
		aintCurrentEats = alEats.toArray( new int[ 0 ][] );
		aoffCurrentOffers = alCurrentOffers.toArray( new Offer[ 0 ] );
		
		System.out.println( "******************************************" );
		System.out.println( "------------------------------------------");
		System.out.println( "Skittles consumption:" );
		for ( int intPlayerIndex = 0; intPlayerIndex < intPlayerNum; intPlayerIndex ++ )
		{
			System.out.print( "Player #" + intPlayerIndex + ": [ " );
			String strInHand = "";
			int[] aintInHand = alEats.get( intPlayerIndex );
			for ( int intInHand : aintInHand )
			{
				strInHand += intInHand + ", ";
			}
			System.out.println( strInHand.substring( 0, strInHand.length() - 2 ) + " ]" );
		}
		System.out.println();
		System.out.println( "All offers:" );
		for ( Offer offTemp : aoffCurrentOffers )
		{
			System.out.println( offTemp.toString() );
		}
		System.out.println( "------------------------------------------");
		System.out.println( "******************************************\n" );
	}
	
	private int[] generateRandomOfferPickOrder()
	{
		ArrayList< Integer > alPlayerIndices = new ArrayList< Integer >();
		for ( int intPlayerIndex = 0; intPlayerIndex < intPlayerNum; intPlayerIndex ++ )
		{
			alPlayerIndices.add( intPlayerIndex );
		}
		int[] aintOrder = new int[ intPlayerNum ];
		Random rdmGenerator = new Random();
		System.out.println( "Random order is:" );
		for ( int intPlayerIndex = 0; intPlayerIndex < intPlayerNum; intPlayerIndex ++ )
		{
			int intRandom = rdmGenerator.nextInt( intPlayerNum - intPlayerIndex );
			aintOrder[ intPlayerIndex ] = alPlayerIndices.get( intRandom );
			alPlayerIndices.remove( intRandom );
			System.out.print( aintOrder[ intPlayerIndex ] + " " );
		}
		System.out.println( "\n" );
		return aintOrder;
	}
	
	private void pickOfferInOrder( int[] aintOrder )
	{
		for ( int intOrderIndex = 0; intOrderIndex < intPlayerNum; intOrderIndex ++ )
		{
			int intPlayerIndex = aintOrder[ intOrderIndex ];
			//skip the player who has eaten all the skittles
			if ( aplsPlayerStatus[ intPlayerIndex ].getTotalInHand() == 0 )
			{
				continue;
			}
			Offer offPicked = aplyPlayers[ intPlayerIndex ].pickOffer( aoffCurrentOffers );
			if ( offPicked != null )
			{
				if ( offPicked.getOfferLive() == false )
				{
					System.out.println( "Offer has been picked, forget about it" );
				}
				else if ( !aplsPlayerStatus[ intPlayerIndex ].checkEnoughInHand( offPicked.getDesire() ) )
				{
					System.out.println( "Player #" + intPlayerIndex + ": you don't have enough skittles to accept this offer. Don't even think about it!" );
				}
				else if ( intPlayerIndex == offPicked.getOfferedByIndex() )
				{
					System.out.println( "Trade with yourself? Schizophrenia..." );
				}
				else
				{
					offPicked.setOfferLive( false );
					int intPickedByIndex = intPlayerIndex;
					int intOfferedByIndex = offPicked.getOfferedByIndex();
					offPicked.setPickedByIndex( intPickedByIndex );
					aplsPlayerStatus[ intOfferedByIndex ].offerExecuted( offPicked );
					aplyPlayers[ intOfferedByIndex ].offerExecuted( offPicked );
					aplsPlayerStatus[ intPickedByIndex ].pickedOffer( offPicked );
					// check after picking an offer, whether the offered offered by intPickedByIndex is still valid. if not, remove it
					Offer offOfferedByPicking = aoffCurrentOffers[ intPickedByIndex ];
					if ( offOfferedByPicking.getOfferLive() && !aplsPlayerStatus[ intPickedByIndex ].checkEnoughInHand( offOfferedByPicking.getOffer() ) )
					{
						offOfferedByPicking.setOfferLive( false );
					}
				}
			}
		}
	}

	private void broadcastOfferExcution()
	{
		for ( int intPlayerIndex = 0; intPlayerIndex < intPlayerNum; intPlayerIndex ++ )
		{
			aplyPlayers[ intPlayerIndex ].updateOfferExe( aoffCurrentOffers );
		}
		
		System.out.println( "\n******************************************" );
		System.out.println( "------------------------------------------");
		System.out.println( "Offer execution: " );
		for ( Offer offTemp : aoffCurrentOffers )
		{
			if ( offTemp.getPickedByIndex() != -1 )
			{
				System.out.println( offTemp.toString() );
			}
		}
		System.out.println( "------------------------------------------");
		System.out.println( "******************************************\n" );
		
	}
	
	public static String arrayToString( int[] aintArray )
	{
		String strReturn = "[ ";
		for ( int intElement : aintArray )
		{
			strReturn += intElement + ", ";
		}
		strReturn = strReturn.substring( 0, strReturn.length() - 2 ) + " ]";
		return strReturn;
	}
}
