package com.lovisgod.sunmip2litese.utils.networkHandler.simplecalladapter;

public interface SimpleHandler<T> {
    void accept(T response, Throwable throwable);
}