car1 = transpose(car_6a40);
car2 = transpose(car_6743);
car3 = transpose(car_673b);
car4 = transpose(car_6a1a);
x = car1(1,1:end);
y = car1(2,1:end);
s = size(car2trans);
plot(x,y, '-o', 'linewidth', 1);
xlim([0 5200]);
ylim([0 5200]);

hold on;
x = car2(1,1:end);
y = car2(2,1:end);
plot(x,y, '-o',  'linewidth', 1);

x = car3(1,1:end);
y = car3(2,1:end);
plot(x,y, '-o',  'linewidth', 1);

x = car4(1,1:end);
y = car4(2,1:end);
plot(x,y, '-o',  'linewidth', 1);

track = imread('track_5200x5200.jpg');
h = image(xlim,ylim,track); 
uistack(h,'bottom');

pbaspect([1 1 1]);

hold off;