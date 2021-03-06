package com.jamieadkins.jgaw;

import com.jamieadkins.jgaw.card.CardDetails;
import com.jamieadkins.jgaw.card.CardStubResult;
import com.jamieadkins.jgaw.exception.GwentApiException;

import java.io.IOException;
import java.util.List;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Public facing api client wrapper.
 */

public class GwentApiClient {
    private static final String API_BASE_URL = "https://api.gwentapi.com/v0/";

    private final GwentApiV0 mGwentApi;

    public GwentApiClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mGwentApi = retrofit.create(GwentApiV0.class);
    }

    public CardDetails getCard(String id) throws IOException, GwentApiException {
        Response<CardDetails> response = mGwentApi.getCard(id).execute();

        if (!response.isSuccessful()) {
            throw new GwentApiException(response.errorBody().string());
        }

        return response.body();
    }

    public List<CardStubResult> getAllCards() throws IOException, GwentApiException {
        Response<CardListResult> response = mGwentApi.getAllCards("200", "0").execute();

        if (!response.isSuccessful()) {
            throw new GwentApiException(response.errorBody().string());
        }

        return response.body().getResults();
    }

    public List<CardStubResult> getAllLeaderCards() throws IOException, GwentApiException {
        Response<CardListResult> response = mGwentApi.getLeaders("200", "0").execute();

        if (!response.isSuccessful()) {
            throw new GwentApiException(response.errorBody().string());
        }

        return response.body().getResults();
    }

    public List<CardStubResult> getCardsFromFaction(String faction)
            throws IOException, GwentApiException {
        Response<CardListResult> response =
                mGwentApi.getCardsFromFaction(faction, "200", "0").execute();

        if (!response.isSuccessful()) {
            throw new GwentApiException(response.errorBody().string());
        }

        return response.body().getResults();
    }

    public List<CardStubResult> getCardsOfRarity(String rarity)
            throws IOException, GwentApiException {
        Response<CardListResult> response =
                mGwentApi.getCardsOfRarity(rarity, "200", "0").execute();

        if (!response.isSuccessful()) {
            throw new GwentApiException(response.errorBody().string());
        }

        return response.body().getResults();
    }

    public static String getIdFromApiUrl(String apiUrl) {
        final int lastSlash = apiUrl.lastIndexOf("/");
        return apiUrl.substring(lastSlash);
    }
}
