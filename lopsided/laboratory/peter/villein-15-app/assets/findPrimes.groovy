def findPrimes(startInt, endInt) {
    x = [];
    for(n in startInt..endInt) {
        prime = true;
        for (i in 3..n-1) {
            if (n % i == 0) {
                prime = false;
                break;
            }
        }
        if (( n%2 !=0 && prime && n > 2) || n == 2) {
            x.add(n);
        }
        meter=(n-startInt)/(endInt-startInt);
        System.out.println(startInt+'-'+endInt + ':n=' + n + 'progress: ' + meter)
        
    }
    return x;
};