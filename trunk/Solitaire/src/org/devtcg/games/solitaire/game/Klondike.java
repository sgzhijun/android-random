package org.devtcg.games.solitaire.game;

import org.devtcg.games.solitaire.R;
import org.devtcg.games.solitaire.model.Card;
import org.devtcg.games.solitaire.model.CardStack;
import org.devtcg.games.solitaire.model.CardStackObserver;
import org.devtcg.games.solitaire.model.Deck;
import org.devtcg.games.solitaire.view.CardStackView;
import org.devtcg.games.solitaire.view.CardView;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class Klondike extends Activity
{
	public static final String TAG = "Klondike";
	
	protected Deck mDeck;
	protected CardStack mDealt;
	protected CardStack[] mTableau;
	protected CardStack[] mFoundation;

	protected CardStackView mDeckView;
	protected CardStackView mDealtView; 
	protected CardStackView[] mFoundationView;
	protected CardStackView[] mTableauView;

	/** Indicates the stack that we are currently holding. */
	protected CardStackView mHolding;

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setContentView(R.layout.klondike);

        initViews();

        if (icicle == null)
        	newGame();
        else
        	loadGame(icicle);
    }
    
    private void initViews()
    {
    	mDeckView = (CardStackView)findViewById(R.id.deck);
    	mDealtView = (CardStackView)findViewById(R.id.dealt);
    	
    	mDeckView.setCardOrientation(CardStackView.Orientation.SINGLE);

        mTableauView = new CardStackView[7];
        mTableauView[0] = (CardStackView)findViewById(R.id.stack1);
        mTableauView[1] = (CardStackView)findViewById(R.id.stack2);
        mTableauView[2] = (CardStackView)findViewById(R.id.stack3);
        mTableauView[3] = (CardStackView)findViewById(R.id.stack4);
        mTableauView[4] = (CardStackView)findViewById(R.id.stack5);
        mTableauView[5] = (CardStackView)findViewById(R.id.stack6);
        mTableauView[6] = (CardStackView)findViewById(R.id.stack7);

        for (int i = 0; i < mTableauView.length; i++)
        {
        	CardStackView view = mTableauView[i];
        	view.setOnClickListener(mTableauClick);
        }

        mFoundationView = new CardStackView[4];
        mFoundationView[0] = (CardStackView)findViewById(R.id.ace1);
        mFoundationView[1] = (CardStackView)findViewById(R.id.ace2);
        mFoundationView[2] = (CardStackView)findViewById(R.id.ace3);
        mFoundationView[3] = (CardStackView)findViewById(R.id.ace4);

        for (int i = 0; i < mFoundationView.length; i++)
        {
        	CardStackView view = mFoundationView[i];        	
        	view.setOnClickListener(mFoundationClick);
        }
    }

    private void newGame()
    {
    	/* Initialize models. */
        mDeck = new Deck();
        mDeck.shuffle();
        mDeckView.connectToCardStack(mDeck, new KlondikeObserver(mDeckView));

        mDealt = new CardStack();
        mDealtView.connectToCardStack(mDealt, new KlondikeObserver(mDealtView));

        mTableau = new CardStack[7];

        for (int i = 0; i < mTableau.length; i++)
        {
        	mTableau[i] = new CardStack(i + 4);
        	mTableau[i].addAll(mDeck.deal(i + 1, false));
        	mTableau[i].peekTop().setFaceUp(true);
        	mTableauView[i].connectToCardStack(mTableau[i],
        	  new KlondikeObserver(mTableauView[i]));
        }

        mFoundation = new CardStack[4];

        for (int i = 0; i < mFoundation.length; i++)
        {
        	mFoundation[i] = new CardStack(13);
    		mFoundationView[i].connectToCardStack(mFoundation[i],
    		  new KlondikeObserver(mFoundationView[i]));
        }

        Log.d(TAG, "Deck:");

        for (int i = 0; i < mDeck.size(); i++)
        	Log.d(TAG, "  Card " + i + ": " + mDeck.get(i));
    }

    private void loadGame(Bundle icicle)
    {
    	/* TODO: Unserialize and load game state. */
    }

    @Override
    protected void onFreeze(Bundle icicle)
    {
    	/* TODO: Serialize and save game state. */
    }
    
    private final OnClickListener mFoundationClick = new OnClickListener()
    {
		public void onClick(View v)
		{
		}
    };

    private final OnClickListener mTableauClick = new OnClickListener()
    {
    	public void onClick(View v)
    	{
			CardStackView vv = (CardStackView)v;

			if (mHolding != null)
			{
				CardStack src = mHolding.getCardStack();
				CardStack dst = vv.getCardStack();

				int pos = findLegalTableauMove(src, dst);

				if (pos >= 0)
				{
					int n = src.size();

					for (int i = pos; i < n; i++)
					{
						Card card = src.remove(pos);
						dst.add(card);
					}

					/* Check that we haven't now removed the top card from this
					 * stack, leaving an unflipped new top. */
					Card top = src.peekTop();

					if (top != null && top.isFaceUp() == false)
						top.setFaceUp(true);
				}

				releaseHolding();
			}
			else
				setHolding(vv);
    	}
    };
    
    /**
     * Checks for an returns a legal move between two tableau stacks.
     *  
     * @param src
     *   Stack to move from.
     *   
     * @param dst
     *   Stack to move to.
     *   
     * @return
     *   If found, the move position in <code>src</code> is returned; otherwise, -1.
     */
    private int findLegalTableauMove(CardStack src, CardStack dst)
    {
    	int srcn = src.size();

    	if (srcn == 0)
    		return -1;

    	Card dsttop = dst.peekTop();

    	int targetOrd = dsttop.getRankOrdinal() - 1;
    	boolean targetIsRed = Card.isSuitBlack(dsttop.getSuit());

    	if (targetOrd < 2)
    		return -1;

    	/* TODO: Optimize. */
    	for (int i = srcn - 1; i >= 0; i--)
    	{
    		Card check = src.get(i);

    		if (check.getRankOrdinal() == targetOrd)
    		{
    			if (targetIsRed == Card.isSuitRed(check.getSuit()))
    				return i;
    			else
    				return -1;
    		}
    	}

    	return -1;
    }

	private void setHolding(CardStackView stack)
	{
		if (mHolding == stack)
			return;

		releaseHolding();
		stack.setSelected(true);
		mHolding = stack;
	}

	private void releaseHolding()
	{
		if (mHolding != null)
		{
			mHolding.setSelected(false);
			mHolding = null;
		}
	}

    public class KlondikeObserver extends CardStackObserver
    {
    	protected CardStackView mView;

    	public KlondikeObserver(CardStackView view)
    	{
    		mView = view;
    	}

		@Override
		protected void onAdd(Card card)
		{
			CardView view = new CardView(Klondike.this);
			view.setCard(card);
			mView.addCard(view);
			Log.d(TAG, mView + ": Added " + card);
			mView.invalidate();
		}

		@Override
		protected void onRemove(int pos)
		{
			CardView view = (CardView)mView.getChildAt(pos);
			mView.removeCard(pos);
			Log.d(TAG, mView + ": Removed " + view.getCard());
			mView.invalidate();
		}
    }
}
