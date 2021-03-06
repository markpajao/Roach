package com.jamieadkins.jgaw;

import com.jamieadkins.jgaw.card.CardDetails;
import com.jamieadkins.jgaw.card.CardStubResult;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

/**
 * GwentApi tests.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class GwentApiTest {
    private static GwentApiClient mApiClient;

    @BeforeClass
    public static void setupGwentApi() {
        mApiClient = new GwentApiClient();
    }

    @Test
    public void getCardTest() throws Exception {
        CardDetails triss = mApiClient.getCard("triss-mistress-of-magic");
        assertNotNull(triss);
    }

    @Test
    public void getCardList() throws Exception {
        List<CardStubResult> cards = mApiClient.getAllCards();
        assertTrue(cards.size() > 0);
    }

    @Test
    public void getNeutralCardList() throws Exception {
        List<CardStubResult> cards = mApiClient.getCardsFromFaction("neutral");
        assertTrue(cards.size() > 0);
    }

    @Test
    public void getLegendaryCardList() throws Exception {
        List<CardStubResult> cards = mApiClient.getCardsOfRarity("legendary");
        assertTrue(cards.size() > 0);
    }

    @Test
    public void getLeaderCardList() throws Exception {
        List<CardStubResult> cards = mApiClient.getAllLeaderCards();
        assertTrue(cards.size() > 0);
    }
}