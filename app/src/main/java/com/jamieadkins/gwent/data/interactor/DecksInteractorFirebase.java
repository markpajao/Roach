package com.jamieadkins.gwent.data.interactor;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.jamieadkins.gwent.data.CardDetails;
import com.jamieadkins.gwent.data.Deck;
import com.jamieadkins.gwent.data.FirebaseUtils;
import com.jamieadkins.gwent.deck.list.DecksContract;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;

/**
 * Deals with firebase.
 */

public class DecksInteractorFirebase implements DecksInteractor {
    private static final String PUBLIC_DECKS_PATH = "public-decks/";

    private DecksContract.Presenter mPresenter;
    private final FirebaseDatabase mDatabase = FirebaseUtils.getDatabase();
    private final DatabaseReference mDecksReference;
    private final DatabaseReference mPublicDecksReference;
    private final Query mDecksQuery;
    private Query mDeckQuery;
    private ChildEventListener mDecksListener;
    private ValueEventListener mDeckDetailListener;

    public DecksInteractorFirebase() {
        this(false);
    }

    public DecksInteractorFirebase(boolean publicDecks) {
        mPublicDecksReference = mDatabase.getReference(PUBLIC_DECKS_PATH);
        if (publicDecks) {
            mDecksReference = mPublicDecksReference;
            mDecksQuery = mDecksReference.orderByChild("week");
        } else {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            mDecksReference = mDatabase.getReference("users/" + userId + "/decks/");
            mDecksQuery = mDecksReference.orderByChild("name");
        }
    }

    @Override
    public void setPresenter(DecksContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public Observable<RxDatabaseEvent<Deck>> getDecks() {
        return Observable.defer(new Callable<ObservableSource<? extends RxDatabaseEvent<Deck>>>() {
            @Override
            public ObservableSource<? extends RxDatabaseEvent<Deck>> call() throws Exception {
                return Observable.create(new ObservableOnSubscribe<RxDatabaseEvent<Deck>>() {
                    @Override
                    public void subscribe(final ObservableEmitter<RxDatabaseEvent<Deck>> emitter) throws Exception {
                        mDecksListener = new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                mPresenter.onLoadingComplete();
                                emitter.onNext(
                                        new RxDatabaseEvent<Deck>(
                                                dataSnapshot.getKey(),
                                                dataSnapshot.getValue(Deck.class),
                                                RxDatabaseEvent.EventType.ADDED));
                            }

                            @Override
                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                                emitter.onNext(
                                        new RxDatabaseEvent<Deck>(
                                                dataSnapshot.getKey(),
                                                dataSnapshot.getValue(Deck.class),
                                                RxDatabaseEvent.EventType.CHANGED));
                            }

                            @Override
                            public void onChildRemoved(DataSnapshot dataSnapshot) {
                                emitter.onNext(
                                        new RxDatabaseEvent<Deck>(
                                                dataSnapshot.getKey(),
                                                dataSnapshot.getValue(Deck.class),
                                                RxDatabaseEvent.EventType.REMOVED));
                            }

                            @Override
                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                                emitter.onNext(
                                        new RxDatabaseEvent<Deck>(
                                                dataSnapshot.getKey(),
                                                dataSnapshot.getValue(Deck.class),
                                                RxDatabaseEvent.EventType.MOVED));
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        };

                        mDecksQuery.addChildEventListener(mDecksListener);
                    }
                });
            }
        });
    }

    @Override
    public Observable<RxDatabaseEvent<Deck>> getDeck(String deckId) {
        mDeckQuery = mDecksReference.child(deckId);
        return Observable.defer(new Callable<ObservableSource<? extends RxDatabaseEvent<Deck>>>() {
            @Override
            public ObservableSource<? extends RxDatabaseEvent<Deck>> call() throws Exception {
                return Observable.create(new ObservableOnSubscribe<RxDatabaseEvent<Deck>>() {
                    @Override
                    public void subscribe(final ObservableEmitter<RxDatabaseEvent<Deck>> emitter) throws Exception {
                        mDeckDetailListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                emitter.onNext(
                                        new RxDatabaseEvent<Deck>(
                                                dataSnapshot.getKey(),
                                                dataSnapshot.getValue(Deck.class),
                                                RxDatabaseEvent.EventType.CHANGED));
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        };

                        mDeckQuery.addValueEventListener(mDeckDetailListener);
                    }
                });
            }
        });
    }

    @Override
    public void stopData() {
        if (mDecksQuery != null && mDecksListener != null) {
            mDecksQuery.removeEventListener(mDecksListener);
        }
        if (mDeckQuery != null && mDeckDetailListener != null) {
            mDeckQuery.removeEventListener(mDeckDetailListener);
        }
    }

    @Override
    public void createNewDeck(String name, String faction, CardDetails leader, String patch) {
        String key = mDecksReference.push().getKey();
        String author = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Deck deck = new Deck(key, name, faction, leader, author, patch);
        Map<String, Object> deckValues = deck.toMap();

        Map<String, Object> firebaseUpdates = new HashMap<>();
        firebaseUpdates.put(key, deckValues);

        mDecksReference.updateChildren(firebaseUpdates);
    }

    @Override
    public void publishDeck(Deck deck) {
        String key = mPublicDecksReference.push().getKey();

        Map<String, Object> deckValues = deck.toMap();
        deckValues.put("id", key);
        deckValues.put("publicDeck", true);
        deckValues.put("week", 0);
        Map<String, Object> firebaseUpdates = new HashMap<>();
        firebaseUpdates.put(key, deckValues);

        mPublicDecksReference.updateChildren(firebaseUpdates);
    }

    @Override
    public void addCardToDeck(Deck deck, final CardDetails card) {
        DatabaseReference deckReference = mDecksReference.child(deck.getId());

        // Transactions will ensure concurrency errors don't occur.
        deckReference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Deck storedDeck = mutableData.getValue(Deck.class);
                if (storedDeck == null) {
                    // No deck with that id, this shouldn't occur.
                    return Transaction.success(mutableData);
                }

                if (storedDeck.getCardCount().containsKey(card.getIngameId())) {
                    // If the user already has at least one of these cards in their deck.
                    int currentCardCount = storedDeck.getCardCount().get(card.getIngameId());
                    storedDeck.getCardCount().put(card.getIngameId(), currentCardCount + 1);
                } else {
                    // Else add one card to the deck.
                    storedDeck.getCardCount().put(card.getIngameId(), 1);
                }

                storedDeck.getCards().put(card.getIngameId(), card);

                // Set value and report transaction success.
                mutableData.setValue(storedDeck);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(getClass().getSimpleName(), "postTransaction:onComplete:" + databaseError);
            }
        });
    }

    @Override
    public void removeCardFromDeck(Deck deck, final CardDetails card) {
        DatabaseReference deckReference = mDecksReference.child(deck.getId());

        // Transactions will ensure concurrency errors don't occur.
        deckReference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Deck storedDeck = mutableData.getValue(Deck.class);
                if (storedDeck == null) {
                    // No deck with that id, this shouldn't occur.
                    return Transaction.success(mutableData);
                }

                if (storedDeck.getCardCount().containsKey(card.getIngameId())) {
                    // If the user already has at least one of these cards in their deck.
                    int currentCardCount = storedDeck.getCardCount().get(card.getIngameId());
                    storedDeck.getCardCount().put(card.getIngameId(), currentCardCount - 1);

                    if (currentCardCount == 0) {
                        storedDeck.getCards().put(card.getIngameId(), null);
                    }
                } else {
                    // This deck doesn't have that card in it.
                }

                // Set value and report transaction success.
                mutableData.setValue(storedDeck);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(getClass().getSimpleName(), "postTransaction:onComplete:" + databaseError);
            }
        });
    }
}
