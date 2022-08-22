package com.mustfaibra.shoesstore.screens.checkout

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberImagePainter
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.mustfaibra.shoesstore.R
import com.mustfaibra.shoesstore.components.CustomButton
import com.mustfaibra.shoesstore.components.DrawableButton
import com.mustfaibra.shoesstore.components.IconButton
import com.mustfaibra.shoesstore.components.SecondaryTopBar
import com.mustfaibra.shoesstore.components.SummaryRow
import com.mustfaibra.shoesstore.models.CartItem
import com.mustfaibra.shoesstore.models.PaymentMethod
import com.mustfaibra.shoesstore.sealed.UiState
import com.mustfaibra.shoesstore.ui.theme.Dimension
import com.mustfaibra.shoesstore.utils.encryptCardNumber
import com.skydoves.whatif.whatIf

@Composable
fun CheckoutScreen(
    cartItems: List<CartItem>,
    onBackRequested: () -> Unit,
    onCheckoutSuccess: () -> Unit,
    onToastRequested: (message: String, color: Color) -> Unit,
    checkoutViewModel: CheckoutViewModel = hiltViewModel(),
) {
    LaunchedEffect(key1 = cartItems) {
        checkoutViewModel.setUserCart(cartItems = cartItems)
    }

    val paymentMethods = checkoutViewModel.paymentMethods
    val selectedPaymentMethodId by remember {
        checkoutViewModel.selectedPaymentMethodId
    }
    val subTotal by remember {
        checkoutViewModel.subTotalPrice
    }
    val checkoutUiState by remember { checkoutViewModel.checkoutState }
    val context = LocalContext.current

    if (checkoutUiState is UiState.Loading) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(
                dismissOnClickOutside = false,
                dismissOnBackPress = false,
            )
        ) {
            Box(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colors.surface)
                    .fillMaxWidth()
                    .padding(Dimension.pagePadding.times(2))
            ) {
                val composition by rememberLottieComposition(
                    spec = LottieCompositionSpec.RawRes(R.raw.world_rounding),
                )

                /** to control the animation speed */
                val progress by animateLottieCompositionAsState(
                    composition,
                    iterations = LottieConstants.IterateForever,
                    speed = 1f,
                    restartOnPlay = true,
                )

                LottieAnimation(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    progress = progress,
                    composition = composition,
                    contentScale = ContentScale.Crop,
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        /** Secondary top bar */
        SecondaryTopBar(
            title = stringResource(id = R.string.checkout),
            onBackClicked = onBackRequested,
        )
        Column(
            modifier = Modifier
                .weight(weight = 1f)
                .verticalScroll(state = rememberScrollState()),
        ) {
            /** Delivery Location */
            DeliveryLocationSection(
                address = "AlTaif 51, st 5",
                city = "Khartoum, Sudan",
                onChangeLocationRequested = {},
            )
            /** Payment methods */
            PaymentMethodsSection(
                methods = paymentMethods,
                selectedPaymentMethodId = selectedPaymentMethodId,
                onMethodChange = { newMethodId ->
                    newMethodId.whatIf(
                        given = { it == selectedPaymentMethodId },
                        whatIfNot = {
                            checkoutViewModel.updateSelectedPaymentMethod(id = newMethodId)
                        },
                        whatIf = {},
                    )
                }
            )
            /** My minimized cart items */
            LazyRow(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimension.pagePadding),
                contentPadding = PaddingValues(Dimension.pagePadding),
            ) {
                items(cartItems) { item ->
                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colors.surface)
                            .aspectRatio(1f),
                    ) {
                        Image(
                            painter = rememberImagePainter(data = item.product?.image),
                            contentDescription = null,
                            modifier = Modifier
                                .size(Dimension.xlIcon)
                                .clip(MaterialTheme.shapes.medium),
                        )
                    }
                }
            }
            /** Checkout summary */
            Column(
                modifier = Modifier
                    .shadow(
                        elevation = Dimension.elevation.div(2),
                        shape = RoundedCornerShape(
                            topStartPercent = 15,
                            topEndPercent = 15,
                        ),
                        spotColor = MaterialTheme.colors.primary,
                    )
                    .clip(shape = MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colors.background)
                    .padding(all = Dimension.pagePadding),
                verticalArrangement = Arrangement.spacedBy(Dimension.sm)
            ) {
                /** sub total cost row */
                SummaryRow(
                    title = stringResource(id = R.string.sub_total),
                    value = "$$subTotal"
                )
                /** shipping cost row */
                SummaryRow(
                    title = stringResource(id = R.string.shipping),
                    value = "$15"
                )
                Divider()
                /** total cost row */
                SummaryRow(
                    title = stringResource(id = R.string.total),
                    value = "$${subTotal.plus(15)}"
                )
                CustomButton(
                    modifier = Modifier
                        .padding(top = Dimension.pagePadding)
                        .fillMaxWidth(),
                    text = stringResource(R.string.pay_now),
                    textStyle = MaterialTheme.typography.body1,
                    buttonColor = MaterialTheme.colors.primary,
                    shape = MaterialTheme.shapes.medium,
                    padding = PaddingValues(
                        all = Dimension.md.times(0.8f),
                    ),
                    onButtonClicked = {
                        checkoutViewModel.makeTransactionPayment(
                            onCheckoutSuccess = onCheckoutSuccess,
                            onCheckoutFailed = { message ->
                                onToastRequested(
                                    context.getString(message),
                                    Color.Red,
                                )
                            }
                        )
                    },
                    contentColor = MaterialTheme.colors.onPrimary,
                )
            }
        }
    }
}

@Composable
fun DeliveryLocationSection(
    address: String,
    city: String,
    onChangeLocationRequested: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(Dimension.pagePadding),
        verticalArrangement = Arrangement.spacedBy(Dimension.pagePadding),
    ) {
        Text(
            text = stringResource(R.string.delivery_address),
            style = MaterialTheme.typography.button,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimension.pagePadding),
        ) {
            DrawableButton(
                painter = painterResource(id = R.drawable.ic_map_pin),
                onButtonClicked = {},
                backgroundColor = MaterialTheme.colors.surface,
                iconTint = MaterialTheme.colors.onSurface,
                paddingValue = PaddingValues(Dimension.sm),
            )
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = address,
                    style = MaterialTheme.typography.body1,
                )
                Text(
                    text = city,
                    style = MaterialTheme.typography.caption,
                )
            }
            IconButton(
                icon = Icons.Rounded.KeyboardArrowRight,
                backgroundColor = MaterialTheme.colors.background,
                iconTint = MaterialTheme.colors.onBackground,
                onButtonClicked = onChangeLocationRequested,
                iconSize = Dimension.mdIcon,
                paddingValue = PaddingValues(Dimension.sm),
                shape = MaterialTheme.shapes.medium,
            )
        }
    }
}

@Composable
fun PaymentMethodsSection(
    methods: List<PaymentMethod>,
    selectedPaymentMethodId: String?,
    onMethodChange: (methodId: String) -> Unit,
) {
    Column(
        modifier = Modifier.padding(Dimension.pagePadding),
        verticalArrangement = Arrangement.spacedBy(Dimension.pagePadding),
    ) {
        Text(
            text = stringResource(R.string.payment_methods),
            style = MaterialTheme.typography.button,
        )
        methods.forEach { method ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimension.pagePadding),
            ) {
                DrawableButton(
                    paddingValue = PaddingValues(Dimension.sm),
                    elevation = Dimension.elevation,
                    painter = painterResource(id = method.icon),
                    onButtonClicked = { },
                    backgroundColor = MaterialTheme.colors.background,
                    shape = MaterialTheme.shapes.medium,
                    iconSize = Dimension.mdIcon,
                )
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = stringResource(id = method.title),
                        style = MaterialTheme.typography.body1,
                    )
                    method.account?.let {
                        Text(
                            text = method.account.encryptCardNumber(),
                            style = MaterialTheme.typography.caption,
                        )
                    }
                }
                RadioButton(
                    selected = method.id == selectedPaymentMethodId,
                    onClick = { onMethodChange(method.id) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colors.secondary,
                        unselectedColor = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                    )
                )
            }
        }
    }
}