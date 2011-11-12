package skittles.g5;

import skittles.sim.*;

public class FatKid extends Player 
{
	private int[] aintInHand;
	private int intColorNum;
	double dblHappiness;
	String strClassName;
	int intPlayerIndex;
	int round=0;
	boolean debugging=false;
	int totalInitialSkittles;
	int skittlesEaten;
	int colorsLeft;
	int colorsUnknownHave;
	int colorsUnknownTotal;
	int maxTransactionSize=Integer.MAX_VALUE;

	private double[] adblTastes;
	private int intLastEatIndex;
	private int intLastEatNum;

	@Override
	public String getClassName() 
	{
		return "DumpPlayer";
	}

	@Override
	public int getPlayerIndex() 
	{
		return intPlayerIndex;
	}



	@Override
	public void initialize(int intPlayerNum, double mean, int intPlayerIndex, String strClassName,	int[] aintInHand) 
	{
		this.intPlayerIndex = intPlayerIndex;
		this.strClassName = strClassName;
		this.aintInHand = aintInHand;
		intColorNum = aintInHand.length;
		dblHappiness = 0;
		adblTastes = new double[ intColorNum ];
		for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
		{
			totalInitialSkittles+=aintInHand[intColorIndex];
			adblTastes[ intColorIndex ] = -2;
			// may have to initialize it to -2 so that we know that we have not tasted it yet
		}
		maxTransactionSize=Integer.MAX_VALUE;
		/*maxTransactionSize=totalInitialSkittles/(intColorNum*3);
		if (maxTransactionSize<4)
			maxTransactionSize=4;
			*/
		// has to be updated 
	}



	@Override
	public void eat( int[] aintTempEat )
	{
		round++;
		double minValue=Double.MAX_VALUE;
		int minValueIndex=Integer.MAX_VALUE;
		int skittlesToEat=0;
		double value=0;
		// the number of skittles in to eat 
		// to be added if skittle is just 1 and taste unknown then avoid eating it
		updateSkittlesInfo();
		for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
		{
			if ( aintInHand[ intColorIndex ] > 0 )
			{ 
				value=adblTastes[intColorIndex]*Math.pow(aintInHand[ intColorIndex ],2);
				if(minValue>value ) {
					minValue=value;
					minValueIndex=intColorIndex;
				}
			}
		}
		skittlesToEat=1;
		if(minValue>0) {
			skittlesToEat=aintInHand[minValueIndex];
			//if(colorsLeft>intColorNum/3)
				//skittlesToEat=1;
			/* this should be the case if no other player is left or no active trading being done
			 if(noActiveTrading())
				skittlesToEat=aintInHand[minValueTasteIndex];
			 */
		}
		aintTempEat[ minValueIndex ] = skittlesToEat;
		aintInHand[ minValueIndex ] -= skittlesToEat;
		intLastEatIndex = minValueIndex;
		intLastEatNum = skittlesToEat;
		skittlesEaten+=skittlesToEat;
		if(debugging) {
			System.out.println("\n Eating by intPlayerIndex="+intPlayerIndex+" in round="+round+" intLastEatIndex="+intLastEatIndex+"  intLastEatNum="+intLastEatNum);
		}
	}

	@Override
	public void offer( Offer offTemp )
	{

		double maxValue=Double.MIN_VALUE;
		double minValue=Double.MAX_VALUE;
		double value; 
		int maxValueTasteIndex=0;
		int minValueTasteIndex=0;
		int transactionSize=0;
		// the number of skittles in offer 
		for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
		{
			if(adblTastes[intColorIndex]!=-2) {

				value=adblTastes[intColorIndex]*Math.pow(aintInHand[ intColorIndex ],2);

				if(maxValue<value) {
					maxValue=value;
					maxValueTasteIndex=intColorIndex;
				}


				if ( aintInHand[ intColorIndex ] > 0 )
				{ 
					if(minValue>value) {
						minValue=value;
						minValueTasteIndex=intColorIndex;
					}
				}
			}
		}
		if(maxTransactionSize<aintInHand[minValueTasteIndex]) 
			transactionSize=maxTransactionSize;
		else 
			transactionSize=aintInHand[minValueTasteIndex];
		
		/*if(transactionSize>3*totalInitialSkittles/intColorNum)
			transactionSize/=3;
			*/
		int[] aintOffer = new int[ intColorNum ];
		int[] aintDesire = new int[ intColorNum ];
		if(minValueTasteIndex!=maxValueTasteIndex) {
			if (maxValue>0) {
				aintOffer[ minValueTasteIndex ] = transactionSize;
				aintDesire[ maxValueTasteIndex ] = transactionSize;
			} else {
				aintOffer[ minValueTasteIndex ] = transactionSize;
				aintDesire[ minValueTasteIndex ] = transactionSize;
			}
		} else {
			if(minValue<0) {
				aintOffer[ minValueTasteIndex ] = transactionSize;
				aintDesire[ minValueTasteIndex ] = transactionSize;
			}
		}
		offTemp.setOffer( aintOffer, aintDesire );
		if(debugging) {
			System.out.println("\nstrClassName="+this.strClassName+"  intPlayerIndex="+intPlayerIndex+" Offer="+minValueTasteIndex+
					"  Desire="+maxValueTasteIndex);
			printArray("aintInHand",aintInHand);
			printArray("adblTastes",adblTastes);
		}
	}

	@Override
	public void happier(double dblHappinessUp) 
	{
		double dblHappinessPerCandy = dblHappinessUp / Math.pow( intLastEatNum, 2 );
		if ( adblTastes[ intLastEatIndex ] == -2 )
		{
			adblTastes[ intLastEatIndex ] = dblHappinessPerCandy;
		}
		else
		{
			if ( adblTastes[ intLastEatIndex ] != dblHappinessPerCandy )
			{
				System.out.println( "Error: Inconsistent color happiness!" );
			}
		}
	}

	@Override
	public Offer pickOffer(Offer[] aoffCurrentOffers) 
	{
		Offer offReturn = null;
		double maxGain=-1;
		for ( Offer offTemp : aoffCurrentOffers )
		{
			if ( offTemp.getOfferedByIndex() == intPlayerIndex || offTemp.getOfferLive() == false )
				continue;
			int[] aintDesire = offTemp.getDesire();
			if ( checkEnoughInHand( aintDesire ) )
			{
				double gainByAccepting = evaluateOffer(offTemp);
				if(debugging) {
					System.out.println("\n for intPlayerIndex="+intPlayerIndex+" gainByAccepting="+gainByAccepting+"  maxGain="+maxGain);
					printArray("we give",offTemp.getDesire());
					printArray("we get",offTemp.getOffer());
				}
				if(gainByAccepting>maxGain && gainByAccepting>0) {
					if(debugging) {
						System.out.println("Above offer is current max\n");
					}
					offReturn = offTemp;
					maxGain=gainByAccepting;

				}
			}
		}
		if( maxGain>0) {
			int[] aintDesire = offReturn.getDesire();
			aintDesire = offReturn.getDesire();
			int[] aintOffer = offReturn.getOffer();
			for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
			{
				aintInHand[ intColorIndex ] += aintOffer[ intColorIndex ] - aintDesire[ intColorIndex ];
			}
		}

		return offReturn;
	}

	@Override
	public void offerExecuted(Offer offPicked) 
	{
		int[] aintOffer = offPicked.getOffer();
		int[] aintDesire = offPicked.getDesire();
		for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
		{
			aintInHand[ intColorIndex ] += aintDesire[ intColorIndex ] - aintOffer[ intColorIndex ];
		}
	}

	@Override
	public void updateOfferExe(Offer[] aoffCurrentOffers) 
	{
		// dumpplayer doesn't care
	}

	@Override
	public void syncInHand(int[] aintInHand) 
	{
		// TODO Auto-generated method stub

	}


	public void updateSkittlesInfo() {
		colorsLeft=0;
		colorsUnknownHave=0;
		colorsUnknownTotal=0;

		for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
		{
			if ( aintInHand[ intColorIndex ] > 0 )
			{ 
				colorsLeft++;
				if(adblTastes[intColorIndex]==-2) {
					colorsUnknownHave++;
					colorsUnknownTotal++;
				}
			} else 
				if(adblTastes[intColorIndex]==-2) 
					colorsUnknownTotal++;
		}
	}

	public void printArray(String message,double[] tempArray) {
		String printingString="";
		for (int i=0;i<tempArray.length;i++) {
			printingString=printingString+" , "+String.format("%+1.5f",tempArray[i]);
		}
		System.out.println(message+"  "+printingString);
	}
	public void printArray(String message,int[] tempArray) {
		String printingString="";
		for (int i=0;i<tempArray.length;i++) {
			printingString=printingString+" , "+String.format("%8d",tempArray[i]);
		}
		System.out.println(message+"  "+printingString);
	}




	public double evaluateOffer( Offer o ) {
		double changeInScore = 0;	
		int[] offer = o.getOffer();
		int[] desire = o.getDesire();
		for (int i=0; i < aintInHand.length; i++) {
			if (adblTastes[i]>0) {
				changeInScore += (adblTastes[i] * Math.pow((offer[i] + aintInHand[i]), 2)) - (adblTastes[i] * Math.pow(aintInHand[i], 2));
				changeInScore -= (adblTastes[i] * Math.pow(aintInHand[i], 2)) - (adblTastes[i] * Math.pow((aintInHand[i]-desire[i]), 2));
			} else {
				changeInScore += (adblTastes[i] * offer[i]);
				changeInScore -= (adblTastes[i] * desire[i]);
				// as adblTastes is negative as well as positive so the final value will correct itself
			}
		}
		return changeInScore;
	}


	private boolean checkEnoughInHand( int[] aintTryToUse )
	{
		for ( int intColorIndex = 0; intColorIndex < intColorNum; intColorIndex ++ )
		{
			if ( aintTryToUse[ intColorIndex ] > aintInHand[ intColorIndex ] )
			{
				return false;
			}
		}
		return true;
	}



}
/*
 *   Things to do (maybe, some ideas might be repetitive)->
 *       1- should we try to trade something which we have never tasted (depending on current info about all which we have )
 *       2- know when the trading is not happening, done with
 *       3- store preferences of other players 
 *       4- some idea of who is having what by noticing the offer execution
 *       5- does it help to be lazy-smart and trade with active-smart ?
 *       6- find matching players ( they need what we don't and we need what they don't) 
 *       7- not to eat if itsthe only skittle and we dont know its taste
 *       8- how often should the offer be repeated
 */

