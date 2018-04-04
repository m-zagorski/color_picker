package com.example.mateusz.colorpicker

import com.jacekmarchwicki.universaladapter.BaseAdapterItem
import com.jacekmarchwicki.universaladapter.ViewHolderManager
import io.reactivex.functions.Consumer


class RxUniversalAdapter(managers: List<ViewHolderManager>): UniversalAdapter2(managers), Consumer<List<BaseAdapterItem>>
