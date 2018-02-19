package io.charlag.moviesdbknot.di

import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.SOURCE

@Qualifier
@Retention(SOURCE)
annotation class ApiKey


@Qualifier
@Retention(SOURCE)
annotation class BaseUrl

/**
 * Used to annotate Android component which need to be injected
 */
interface Injectable