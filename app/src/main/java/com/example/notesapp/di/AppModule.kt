@file:Suppress("DEPRECATION")

package com.example.notesapp.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.notesapp.data.repository.AuthRepositoryImpl
import com.example.notesapp.data.repository.NotesRepositoryImpl
import com.example.notesapp.domain.repository.AuthRepository
import com.example.notesapp.domain.repository.NotesRepository
import com.example.notesapp.domain.usecase.auth.GoogleSignInUseCase
import com.example.notesapp.domain.usecase.auth.LoginUseCase
import com.example.notesapp.domain.usecase.auth.RegisterUseCase
import com.example.notesapp.domain.usecase.notes.AddCategoryUseCase
import com.example.notesapp.domain.usecase.notes.AddNoteUseCase
import com.example.notesapp.domain.usecase.notes.DeleteCategoryUseCase
import com.example.notesapp.domain.usecase.notes.DeleteNoteUseCase
import com.example.notesapp.domain.usecase.notes.GetCategoriesUseCase
import com.example.notesapp.domain.usecase.notes.GetNotesUseCase
import com.example.notesapp.domain.usecase.notes.UpdateNoteUseCase
import com.example.notesapp.presentation.viewmodel.AuthViewModel
import com.example.notesapp.presentation.viewmodel.CategoryViewModel
import com.example.notesapp.presentation.viewmodel.NotesViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }

    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }

    factory<NotesRepository> {
        val auth = get<FirebaseAuth>()
        val userId = auth.currentUser?.uid ?: ""
        NotesRepositoryImpl(db = get(), userId = userId)
    }

    factory { LoginUseCase(get()) }
    factory { RegisterUseCase(get()) }
    factory { GoogleSignInUseCase(get()) }

    factory { AddCategoryUseCase(get()) }
    factory { GetCategoriesUseCase(get()) }
    factory { DeleteCategoryUseCase(get()) }
    factory { AddNoteUseCase(get()) }
    factory { GetNotesUseCase(get()) }
    factory { UpdateNoteUseCase(get()) }
    factory { DeleteNoteUseCase(get()) }

    viewModel { AuthViewModel(get(), get(), get()) }
    viewModel { CategoryViewModel(get(), get(), get()) }
    viewModel { (categoryId: String) -> NotesViewModel(get(), get(), get(), get(), categoryId) }
}