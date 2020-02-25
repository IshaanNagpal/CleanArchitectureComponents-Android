package com.sample.gitrepos.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.sample.gitrepos.network.Resource
import com.sample.gitrepos.usecases.ReposListResposListUseCaseImpl
import com.sample.gitrepos.utility.ListItemModel
import com.sample.gitrepos.utility.REPOS_ACTIVITY_STATE
import com.sample.gitrepos.views.ReposItemView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReposListViewModelImpl(mApplication: Application, private val reposListUseCaseImpl: ReposListResposListUseCaseImpl, private val savedStateHandle: SavedStateHandle) : BaseViewModel(mApplication), ReposListViewModel {

    private val reposListLiveData by lazy { MutableLiveData<List<ListItemModel>>() }

    override fun getReposLiveData(): MutableLiveData<List<ListItemModel>> = savedStateHandle.getLiveData(REPOS_ACTIVITY_STATE)

    init {
        observeForReposResponseLiveData()
    }

    private fun observeForReposResponseLiveData() {
        reposListUseCaseImpl.subscribeForReposData().observeForever {
            when(it.status) {

                Resource.Status.LOADING -> {setLoading()}

                Resource.Status.SUCCESS -> {
                    val reposItemViewList by lazy { mutableListOf<ListItemModel>() }
                    val reposListModel = it.data
                    reposListModel?.map { gitReposModel -> reposItemViewList.add(ReposItemView(gitReposModel))
                        savedStateHandle.set(REPOS_ACTIVITY_STATE,reposItemViewList)
                        reposListLiveData.postValue(reposItemViewList)
                        setSuccess()
                    }
                }

                Resource.Status.ERROR -> {setError()}
            }
        }
    }

    fun showReposData() {
        viewModelScope.launch(Dispatchers.Main) {
            reposListUseCaseImpl.getDataFromRepository()
        }
    }

}