package com.app.nao.photorecon.ui.global;
import static androidx.lifecycle.SavedStateHandleSupport.createSavedStateHandle;
import static androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY;

import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.viewmodel.ViewModelInitializer;

import com.app.nao.photorecon.CustomApplication;

// NOTE:Viewよりも大きいライフサイクルで動くので今のアプリでは，View依存のstateだけで問題ない．
// NOTE:回転等の外因的な要因に強くなるが，Albumがメモリをたくさん使うので，
// アプリのライフサイクルを通して可能な限りグローバルなstateを持たないように今回はする．
public class GlobalViewModel extends ViewModel {
    public GlobalViewModel(
            GlobalState state,
            SavedStateHandle savedStateHandle
    ) { /* Init ViewModel here */ }

    static final ViewModelInitializer<GlobalViewModel> initializer = new ViewModelInitializer<>(
            GlobalViewModel.class,
            creationExtras -> {
                CustomApplication app = (CustomApplication) creationExtras.get(APPLICATION_KEY);
                assert app != null;
                SavedStateHandle savedStateHandle = createSavedStateHandle(creationExtras);

                return new GlobalViewModel(app.getMyRepository(), savedStateHandle);
            }
    );

}
