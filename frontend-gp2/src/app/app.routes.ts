import { Routes } from '@angular/router';
import { MainLayoutComponent } from './shared/layouts/main-layout/main-layout.component';
import { HomeComponent } from './features/home/home.component';
import { ProductListComponent } from './features/products/product-list/product-list.component';

export const routes: Routes = [
{
    path:'',
    component: MainLayoutComponent,
    children:[
        {path:'home', component:HomeComponent},
        {path:'productos', component:ProductListComponent}
    ]
},
{
    path:'**', redirectTo:'home'
}

];